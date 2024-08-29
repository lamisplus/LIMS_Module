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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    public LIMSResult Save(LIMSResult result){
        List<LIMSResult> limsResults = resultRepository.findAllBySampleID(result.getSampleID());

        if(limsResults.size() == 0) {
            if(result.getTestResult().length() > 0) {
                LOG.info("1. RESULT: " + result);
                result.setUuid(UUID.randomUUID().toString());
                SaveResultInLabModule(result);
                LOG.info("SAVING RESULT: Result saved successfully in Lab Module");
                return resultRepository.save(result);
            }
            else{
                LOG.info("SAVING RESULT: Result not saved, object has no result value");
                return result;
            }
        }
        else{
            LOG.info("SAVING RESULT: Result not saved, already saved before");
            return null;
        }
    }

    public List<LIMSResult> SaveAll(List<LIMSResult> results){
        List<LIMSResult> savedResults = new ArrayList<>();

        for(LIMSResult result:results){
            LIMSResult savedResult = Save(result);
            savedResults.add(savedResult);
        }

        return  savedResults;
    }

    public LIMSResult Update(LIMSResult result, int id){
        return resultRepository.save(result);
    }

    public LIMSResult FindById(int id){
        return resultRepository.findById(id).orElse(null);
    }

    public  String Delete(int id){
        resultRepository.deleteById(id);
        return id+" deleted successfully";
    }

    public ManifestDTO FindResultsByManifestId(Integer id){
        ManifestDTO dto = limsMapper.toManifestDto(manifestRepository.findById(id).orElse(null));
        List<LIMSResult> results = resultRepository.findAllByManifestRecordID(id);
        dto.setResults(results);
        return dto;
    }

    public void SaveResultInLabModule(LIMSResult result){
        try {
            LIMSTest test = testRepository.findBySampleId(result.getSampleID()).get(0);
            LOG.info("LAB TEST: " + test);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            resultRepository.SaveSampleResult(UUID.randomUUID().toString(),
                    LocalDateTime.parse(result.getAssayDate()+" 00:00:00", formatter),
                    LocalDateTime.parse(result.getDateResultDispatched()+" 00:00:00", formatter),
                    LocalDateTime.now(),
                    result.getTestResult(),
                    test.getId(),
                    test.getPatientUuid(),
                    Math.toIntExact(test.getFacilityId()),
                    test.getPatientId());
            resultRepository.UpdateTestStatus(test.getId());
        }catch (Exception exception) {
            LOG.info("ERROR SAVING RESULT IN LAB MODULE: " + exception.getMessage());
        }
    }
}
