package org.lamisplus.modules.lims.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.lamisplus.modules.base.domain.dto.PageDTO;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LimsSampleRepository;
import org.lamisplus.modules.lims.util.JsonNodeTransformer;
import org.lamisplus.modules.patient.domain.dto.PersonResponseDto;
import org.lamisplus.modules.patient.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LimsSampleService {
    private final LimsSampleRepository sampleRepository;
    private final LimsMapper limsMapper;
    private final PersonService personService;
    private  final UserService userService;
    private final JsonNodeTransformer jsonNodeTransformer;

    public LABSampleDTO Save(LABSampleDTO sampleDTO){
        LIMSSample sample = limsMapper.toSample(sampleDTO);
        sample.setUuid(UUID.randomUUID().toString());
        return limsMapper.tosSampleDto( sampleRepository.save(sample));
    }

    public LABSampleDTO Update(LABSampleDTO sampleDTO){
        return Save(sampleDTO);
    }

    public String Delete(Integer id){
        LIMSSample sample = sampleRepository.findById(id).orElse(null);
        sampleRepository.delete(sample);
        return id.toString() + " deleted successfully";
    }

    public LABSampleDTO findById(Integer id) {
        return limsMapper.tosSampleDto(sampleRepository.findById(id).orElse(null));
    }

    public List<LABSampleDTO> findbyManifestRecordId(int id) {
        return AppendPatientDetails(limsMapper.toSampleDtoList(sampleRepository.findAllByManifestRecordID(id)));
    }

    public LABSampleMetaDataDTO getAllPendingSamples(String searchParam, int pageNo, int pageSize) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());

        Page<LIMSSample> limsSamples = sampleRepository.findPendingVLSamples(getCurrentUserOrganization(), paging);
        return getManifestListMetaDataDto(limsSamples);
    }

    @Nullable
    private LABSampleMetaDataDTO getManifestListMetaDataDto(Page<LIMSSample> samples) {
        List<LABSampleDTO> sampleDTOS = limsMapper.toSampleDtoList(samples.getContent());

        if (samples.hasContent()) {
            PageDTO pageDTO = this.generatePagination(samples);
            LABSampleMetaDataDTO labSampleMetaDataDTO = new LABSampleMetaDataDTO();
            labSampleMetaDataDTO.setTotalRecords(pageDTO.getTotalRecords());
            labSampleMetaDataDTO.setPageSize(pageDTO.getPageSize());
            labSampleMetaDataDTO.setTotalPages(pageDTO.getTotalPages());
            labSampleMetaDataDTO.setCurrentPage(pageDTO.getPageNumber());
            labSampleMetaDataDTO.setRecords(AppendPatientDetails(sampleDTOS));
            return labSampleMetaDataDTO;
        }

        return new LABSampleMetaDataDTO();
    }

    public PageDTO generatePagination(Page page) {
        long totalRecords = page.getTotalElements();
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();
        return PageDTO.builder().totalRecords(totalRecords)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages).build();
    }

    public List<LABSampleDTO> AppendPatientDetails(List<LABSampleDTO> sampleDTOS) {
        for (LABSampleDTO sampleDTO: sampleDTOS) {
            PersonResponseDto personResponseDTO = personService.getPersonById((long) sampleDTO.getPid());

            List<PatientIdDTO> patientIdDTOS = new ArrayList<>();
            PatientIdDTO patientIdDTO = new PatientIdDTO();
            patientIdDTO.setIdNumber(jsonNodeTransformer.getNodeValue(personResponseDTO.getIdentifier(), "identifier", "value", true));
            patientIdDTO.setIdTypeCode("HOSPITALNO");
            patientIdDTOS.add(patientIdDTO);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode patientIDNode = mapper.convertValue(patientIdDTO, JsonNode.class);

            sampleDTO.setPatientID(patientIDNode);
            sampleDTO.setAge("10");
            sampleDTO.setDateSampleSent("");
            sampleDTO.setArtCommencementDate("");
            sampleDTO.setDateOfBirth(String.valueOf(personResponseDTO.getDateOfBirth()));
            sampleDTO.setDrugRegimen("");
            sampleDTO.setFirstName(personResponseDTO.getFirstName());
            sampleDTO.setSurName(personResponseDTO.getSurname());
            sampleDTO.setIndicationVLTest("1");
            sampleDTO.setPregnantBreastFeedingStatus("");
            sampleDTO.setSex("M");
        }

        return sampleDTOS;
    }

    public Long getCurrentUserOrganization() {
        Optional<User> userWithRoles = userService.getUserWithRoles ();
        return userWithRoles.map (User::getCurrentOrganisationUnitId).orElse (null);
    }
}
