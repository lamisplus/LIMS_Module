package org.lamisplus.modules.lims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.ManifestDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSResult;
import org.lamisplus.modules.lims.domain.entity.LIMSTest;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LimsManifestRepository;
import org.lamisplus.modules.lims.repository.LimsResultRepository;
import org.lamisplus.modules.lims.repository.LimsTestRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LimsResultService {
    private final LimsResultRepository limsResultRepository;
    private final LimsManifestRepository manifestRepository;
    private final LimsTestRepository testRepository;
    private final LimsMapper limsMapper;
    private final CurrentFacility currentFacility;


    public LIMSResult Save(LIMSResult result, String hospitalNumber) {
        LOG.info("Transaction Name: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        String personUuid = testRepository.getPersonUuidByHospitalNum(hospitalNumber)
                .orElseThrow(() -> new RuntimeException("Person UUID not found with hospital number: " + hospitalNumber));

        if (result.getTestResult().isEmpty()) {
            return null;
        }
        result.setUuid(UUID.randomUUID().toString());
        boolean isSaved = saveResultInLabModule(result, personUuid);

        if (!isSaved) {
            LOG.info("SAVING RESULT: Result not saved, sample has no result value");
            return null;
        }
        LOG.info("SAVING RESULT: Result saved successfully in Lab Module");
        List<LIMSResult> previousResult = limsResultRepository
                .getLIMSResultByManifestRecordIdAndSampleId(result.getManifestRecordID(), result.getSampleID());

        return previousResult.isEmpty() ? limsResultRepository.save(result) : result;
    }


    public LIMSResult Update(LIMSResult result, int id) {
        return limsResultRepository.save(result);
    }

    public LIMSResult FindById(int id) {
        return limsResultRepository.findById(id).orElse(null);
    }

    public String Delete(int id) {
        limsResultRepository.deleteById(id);
        return id + " deleted successfully";
    }

    public ManifestDTO FindResultsByManifestId(Integer id) {
        ManifestDTO dto = limsMapper.toManifestDto(manifestRepository.findById(id).orElse(null));
        List<LIMSResult> results = limsResultRepository.findAllByManifestRecordID(id);
        dto.setResults(results);
        return dto;
    }


    public boolean saveResultInLabModule(LIMSResult result, String personUuid) {
        try {
            String processedTestResult = extractNumericValue(result.getTestResult());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return processTestResult(result, personUuid, processedTestResult, formatter);
        } catch (Exception exception) {
            LOG.error("Error saving result in lab module: {}", exception.getMessage());
            return false;
        }
    }

    private boolean processTestResult(LIMSResult result, String personUuid, String testResult, DateTimeFormatter formatter) {
        if (result.getTestID() != null) {
            return handleExistingTest(result, personUuid, testResult, formatter);
        }
        return handleNewTest(result, personUuid, testResult, formatter);
    }

    private boolean handleExistingTest(LIMSResult result, String personUuid, String testResult, DateTimeFormatter formatter) {
        Integer testId = result.getTestID();
        LIMSTest limsTest = testRepository.findByTestId(testId);
        Integer patientId = limsTest.getPatientId();
        if (limsResultRepository.existsByTestId(testId)) {
            updateResultFields(result, testId, testResult, formatter);
        } else {
            LOG.info("No result instance found with test_id {}", testId);
            insertLabResult(patientId, personUuid, result, testId, testResult, formatter);
        }
        testRepository.updateLabTestOrderStatusToFive(limsTest.getId());
        return true;
    }

    private boolean handleNewTest(LIMSResult result, String personUuid, String testResult, DateTimeFormatter formatter) {
        LIMSTest test = testRepository.findBySampleIdAndPersonUuid(result.getSampleID(), personUuid)
                .orElseThrow(() -> new RuntimeException("Lab Test not found for PersonUUID: " + personUuid));
        Integer patientId = test.getPatientId();
        Integer labTestId = test.getLabTestId();
        if (limsResultRepository.existsByTestId(labTestId)) {
            updateResultFields(result, labTestId, testResult, formatter);
        } else {
            LOG.info("No result instance found with test_id {}", labTestId);
            insertLabResult(patientId, personUuid, result, labTestId, testResult, formatter);
        }
        //update test order status
        testRepository.updateLabTestOrderStatusToFive(test.getId());
        return true;
    }


    public void updateResultFields(LIMSResult result, Integer testId, String testResult, DateTimeFormatter formatter) {
        LocalDateTime assayDate = LocalDateTime.parse(result.getAssayDate() + " 00:00:00", formatter);
        LocalDateTime reportedDate = LocalDateTime.parse(result.getResultDate() + " 00:00:00", formatter);
        LocalDateTime dateResultDispatched = LocalDateTime.parse(result.getDateResultDispatched() + " 00:00:00", formatter);
        String pcrLabSampleNumber = result.getPcrLabSampleNumber();
        String approvedBy = result.getApprovedBy();
        limsResultRepository.updateLabResultNative(
                testResult,
                reportedDate,
                assayDate,
                dateResultDispatched,
                pcrLabSampleNumber,
                approvedBy,
                testId
        );
    }

    public void insertLabResult(Integer patientId, String personUuid, LIMSResult result, Integer testId, String testResult, DateTimeFormatter formatter) {
        LocalDateTime assayDate = LocalDateTime.parse(result.getAssayDate() + " 00:00:00", formatter);
        LocalDateTime reportedDate = LocalDateTime.parse(result.getResultDate() + " 00:00:00", formatter);
        LocalDateTime dateResultDispatched = LocalDateTime.parse(result.getDateResultDispatched() + " 00:00:00", formatter);
        String pcrLabSampleNumber = result.getPcrLabSampleNumber();
        String approvedBy = result.getApprovedBy();
        String labResultUuid = UUID.randomUUID().toString();
        long facilityId = currentFacility.getCurrentUserOrganization();
        limsResultRepository.insertLabResultNative(
                labResultUuid,
                facilityId,
                testId,
                patientId,
                personUuid,
                testResult,
                reportedDate,
                assayDate,
                dateResultDispatched,
                pcrLabSampleNumber,
                approvedBy
        );
    }


    /**
     * Extracts numeric parts from a test result string.
     * 
     * Side effects:
     * 1. If input is "NotDetected" (case-insensitive), returns "0"
     * 2. If input is null or empty, returns null
     * 3. Extracts only digits and at most one decimal point from the input
     * 4. If no numeric part is found or only a decimal point is found, returns null
     * 5. Non-numeric characters (except one decimal point) are removed
     * 6. If multiple decimal points exist, only the first one is kept
     * 
     * @param resultString The test result string to process
     * @return The extracted numeric value as a string, or null if no valid numeric value found
     */
    public static String extractNumericValue(String resultString) {
        if (resultString != null && resultString.equalsIgnoreCase("NotDetected")) {
            resultString = "0";
            return resultString;
        }

        if (resultString == null || resultString.isEmpty()) {
            return null;
        }

        StringBuilder numericPart = new StringBuilder();
        boolean decimalFound = false;

        for (int i = 0; i < resultString.length(); i++) {
            char c = resultString.charAt(i);
            if (Character.isDigit(c)) {
                numericPart.append(c);
            } else if (c == '.' && !decimalFound) {
                numericPart.append(c);
                decimalFound = true;
            }
        }

        if (numericPart.length() == 0 || numericPart.toString().equals(".")) {
            return null;
        }

        return numericPart.toString();
    }

    public LIMSResult getSampleResultBySampleId(String sampleId) {
        String manifestSampleId = null;
        if (sampleId.contains("_")) {
            manifestSampleId = sampleId.replace("_", "/");
        } else {
            manifestSampleId = sampleId;
        }
        if (limsResultRepository.getLIMSResultBySampleID(manifestSampleId).isPresent()) {
            System.out.println(manifestSampleId);
            return limsResultRepository.getLIMSResultBySampleID(manifestSampleId).get();
        }
        return null;
    }

    public LIMSTest getPatientIDBySampleID(@Param("sampleId") String sampleId) {
        String manifestSampleId = null;
        if (sampleId.contains("_")) {
            manifestSampleId = sampleId.replace("_", "/");
        } else {
            manifestSampleId = sampleId;
        }
        return testRepository.findBySampleId(manifestSampleId).get(0);
    }


}
