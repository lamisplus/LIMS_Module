package org.lamisplus.modules.lims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.laboratory.domain.entity.Result;
import org.lamisplus.modules.laboratory.repository.ResultRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LimsResultService {
    private final LimsResultRepository resultRepository;
    private final LimsManifestRepository manifestRepository;
    private final LimsTestRepository testRepository;
    private final LimsMapper limsMapper;
    private final ResultRepository labResultRepository;


    public LIMSResult Save(LIMSResult result, String hospitalNumber) {
        Optional<String> personUuid = testRepository.getPersonUuidByHospitalNum(hospitalNumber);
        if (!personUuid.isPresent()) {
            throw new RuntimeException("Person UUID not found with give hospitalNumber   " + hospitalNumber);
        }
        if (result.getTestResult().length() > 0) {
            result.setUuid(UUID.randomUUID().toString());
            SaveResultInLabModule(result, personUuid.get());
            LOG.info("SAVING RESULT: Result saved successfully in Lab Module");
            List<LIMSResult> previousResult =
                    resultRepository.getLIMSResultByManifestRecordIdAndSampleId(result.getManifestRecordID(), result.getSampleID());
            if (previousResult.isEmpty()) {
                return resultRepository.save(result);
            } else {
                return result;
            }
        } else {
            LOG.info("SAVING RESULT: Result not saved, object has no result value");
            return result;
        }
    }

//    public List<LIMSResult> SaveAll(List<LIMSResult> results) {
//        List<LIMSResult> savedResults = new ArrayList<>();
//
//        for (LIMSResult result : results) {
//            LIMSResult savedResult = Save(result);
//            savedResults.add(savedResult);
//        }
//
//        return savedResults;
//    }

    public LIMSResult Update(LIMSResult result, int id) {
        return resultRepository.save(result);
    }

    public LIMSResult FindById(int id) {
        return resultRepository.findById(id).orElse(null);
    }

    public String Delete(int id) {
        resultRepository.deleteById(id);
        return id + " deleted successfully";
    }

    public ManifestDTO FindResultsByManifestId(Integer id) {
        ManifestDTO dto = limsMapper.toManifestDto(manifestRepository.findById(id).orElse(null));
        List<LIMSResult> results = resultRepository.findAllByManifestRecordID(id);
        dto.setResults(results);
        return dto;
    }

    public void SaveResultInLabModule(LIMSResult result, String personUuid) {
        try {
            LIMSTest test = null;
            boolean testIdExists = result.getTestID() != null;
            String testResult = result.getTestResult();
            testResult = extractCopyNumber(testResult);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (testIdExists) {
                Integer testID = result.getTestID();
                List<Result> labResults = labResultRepository.findAllByTestIdAndArchived(testID, 0);
                if (!labResults.isEmpty()) {
                    updateLabResult(result, labResults, testResult, formatter);
                }
            } else {
                test = testRepository.findBySampleIdAndPersonUuid(result.getSampleID(), personUuid).orElse(null);
                if (test != null) {
                    Integer labTestId = test.getLabTestId();
                    List<Result> labResults = labResultRepository.findAllByTestIdAndArchived(labTestId, 0);
                    if (!labResults.isEmpty()) {
                        updateLabResult(result, labResults, testResult, formatter);
                    } else {
                        throw new RuntimeException("Lab Test not found with given PersonUUId   " + personUuid);
                    }
                }
            }
        } catch (Exception exception) {

            LOG.info("ERROR SAVING RESULT IN LAB MODULE: " + exception.getMessage());
        }
    }

    private void updateLabResult(LIMSResult result, List<Result> labResults, String testResult, DateTimeFormatter formatter) {
        Result labResult = labResults.get(0);
        labResult.setResultReport(testResult);
        labResult.setResultReported(testResult);
        labResult.setDateAssayed(LocalDateTime.parse(result.getAssayDate() + " 00:00:00", formatter));
        labResult.setDateResultReported(LocalDateTime.parse(result.getDateResultDispatched() + " 00:00:00", formatter));
        labResult.setDateResultReceived(LocalDateTime.now());
        labResult.setPcrLabSampleNumber(result.getPcrLabSampleNumber());
        labResult.setApprovedBy(result.getApprovedBy());
        labResult.setDateApproved(LocalDate.parse(result.getDateResultDispatched() + " 00:00:00", formatter));
        labResult.setDateSampleReceivedAtPcrLab(LocalDate.parse(result.getDateSampleReceivedAtPcrLab() + " 00:00:00", formatter));
        labResultRepository.save(labResult);
    }

    public static String extractCopyNumber(String resultString) {
        if (resultString != null && resultString.equalsIgnoreCase("NotDetected")) {
            resultString = "0";
            return resultString;

        }
        if (resultString == null || resultString.isEmpty()) {
            return null;
        }
        StringBuilder numericPart = new StringBuilder();
        for (int i = 0; i < resultString.length(); i++) {
            char c = resultString.charAt(i);
            if (Character.isDigit(c)) {
                numericPart.append(c);
            }
        }
        if (numericPart.length() == 0) {
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
        if (resultRepository.getLIMSResultBySampleID(manifestSampleId).isPresent()) {
            System.out.println(manifestSampleId);
            return resultRepository.getLIMSResultBySampleID(manifestSampleId).get();
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
