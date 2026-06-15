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

import java.time.LocalDate;
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
            LOG.info("test result.. " + result.getTestResult());
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
        try{
            Integer testId = result.getTestID();
            LIMSTest limsTest = testRepository.findByTestId(testId);
            Integer patientId = limsTest.getPatientId();
            if (limsResultRepository.existsByTestId(testId)) {
                LOG.info("Updating Lab results.. for test " + limsTest);
                updateResultFields(result, testId, testResult, formatter);
            } else {
                LOG.info("No result instance found with test_id {}", testId);
                insertLabResult(patientId, personUuid, result, testId, testResult, formatter);
            }
            testRepository.updateLabTestOrderStatusToFive(limsTest.getId());
        }catch (Exception exception) {
            LOG.error("Error msg " + exception);
        }

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

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date);
    }

    public void updateResultFields(LIMSResult result, Integer testId, String testResult, DateTimeFormatter formatter) {
        String limsAssayDate = result.getAssayDate();
        String limsResultDate = result.getResultDate();
        LocalDateTime assayDate;
        LocalDateTime reportedDate;

        LocalDate today = LocalDate.now();

        if (limsAssayDate == null || limsAssayDate.isEmpty()) {
            assayDate = LocalDateTime.parse(result.getVisitDate() + " 00:00:00", formatter);
        }else {
            LocalDate parsedDate = parseDate(limsAssayDate);
            if (parsedDate.isAfter(today)) {
                assayDate = LocalDateTime.parse(result.getApprovalDate() + " 00:00:00", formatter);
            }else{
                assayDate = LocalDateTime.parse(limsAssayDate + " 00:00:00", formatter);
            }
        }

        if (limsResultDate == null || limsResultDate.isEmpty()) {
            reportedDate = LocalDateTime.parse(result.getApprovalDate() + " 00:00:00", formatter);
        }else {
            LocalDate parsedDate = parseDate(limsResultDate);
            if (parsedDate.isAfter(today)) {
                reportedDate = LocalDateTime.parse(result.getDateResultDispatched() + " 00:00:00", formatter);
            }else{
                reportedDate = LocalDateTime.parse(limsResultDate + " 00:00:00", formatter);
            }

        }

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
        String limsAssayDate = result.getAssayDate();
        String limsResultDate = result.getResultDate();
        LocalDateTime assayDate;
        LocalDateTime reportedDate;

        LocalDate today = LocalDate.now();

        if (limsAssayDate == null || limsAssayDate.isEmpty()) {
            assayDate = LocalDateTime.parse(result.getVisitDate() + " 00:00:00", formatter);
        }else {
            LocalDate parsedDate = parseDate(limsAssayDate);
            if (parsedDate.isAfter(today)) {
                assayDate = LocalDateTime.parse(result.getApprovalDate() + " 00:00:00", formatter);
            }else{
                assayDate = LocalDateTime.parse(limsAssayDate + " 00:00:00", formatter);
            }
        }

        if (limsResultDate == null || limsResultDate.isEmpty()) {
            reportedDate = LocalDateTime.parse(result.getApprovalDate() + " 00:00:00", formatter);
        }else {
            LocalDate parsedDate = parseDate(limsResultDate);
            if (parsedDate.isAfter(today)) {
                reportedDate = LocalDateTime.parse(result.getDateResultDispatched() + " 00:00:00", formatter);
            }else{
                reportedDate = LocalDateTime.parse(limsResultDate + " 00:00:00", formatter);
            }
        }
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

    public static String extractNumericValue(String resultString) {
        if (resultString == null || resultString.trim().isEmpty()) {
            return null;
        }

        resultString = resultString.trim();

        // Remove comparison symbols <, >, ≤, ≥ and extra spaces
        resultString = resultString.replaceAll("^[<>]=?|\\s+", "");

        if (resultString.equalsIgnoreCase("NotDetected") ||
                resultString.equalsIgnoreCase("TargetNotDetected") ) {
            return "0";
        }

//        if (resultString.equalsIgnoreCase("TargetNotDetected")) {
//            return "9";
//        }

        if (resultString.equalsIgnoreCase("Titermin")) {
            return "10";
        }


        StringBuilder numericPart = new StringBuilder();
        boolean decimalFound = false;

        for (char c : resultString.toCharArray()) {
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

        try {
            double value = Double.parseDouble(numericPart.toString());
            long roundedValue = Math.round(value);
            return String.valueOf(roundedValue);
        } catch (NumberFormatException e) {
            return null;
        }
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
