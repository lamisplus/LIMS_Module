package org.lamisplus.modules.lims.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.lamisplus.modules.base.domain.dto.PageDTO;
import org.lamisplus.modules.base.domain.entities.OrganisationUnit;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.OrganisationUnitService;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.domain.entity.*;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.*;
import org.lamisplus.modules.patient.domain.dto.PersonMetaDataDto;
import org.lamisplus.modules.patient.domain.dto.PersonResponseDto;
import org.lamisplus.modules.patient.domain.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.jar.Manifest;

@Service
@Slf4j
@RequiredArgsConstructor
public class LimsManifestService {
    private final LimsManifestRepository limsManifestRepository;
    private final LimsResultRepository resultRepository;
    private final LimsSampleRepository sampleRepository;
    private final LimsMapper limsMapper;
    private final OrganisationUnitService organisationUnitService;
    private  final UserService userService;
    private final LimsResultService resultService;
    private final LimsConfigRepository limsConfigRepository;

    String loginUrl = "/login.php";
    String manifestUrl = "/samples/create.php";
    String resultsUrl = "/samples/result.php";

    public ManifestDTO Save(ManifestDTO manifestDTO){
        LIMSManifest manifest = limsMapper.tomManifest(manifestDTO);

        if(manifest.getId()==0) {
            Long FacilityId = getCurrentUserOrganization();
            OrganisationUnit organisationUnit = organisationUnitService.getOrganizationUnit(FacilityId);
            String FacilityName = organisationUnit.getName();
            String FacilityDATIMCode = "";
            String FacilityMFLCode ="54321";

            try {
                FacilityDATIMCode = Objects.requireNonNull(organisationUnit.getOrganisationUnitIdentifiers().stream()
                        .filter(x -> "DATIM_ID".equals(x.getName())).findFirst().orElse(null)).getCode();
            }catch (Exception ignored){

            }
            try {
                FacilityMFLCode = Objects.requireNonNull(organisationUnit.getOrganisationUnitIdentifiers().stream()
                        .filter(x -> "MFL_ID".equals(x.getName())).findFirst().orElse(null)).getCode();
            }catch (Exception ignored){

            }

            manifest.setManifestID(GenerateManifestID(FacilityDATIMCode));
            manifest.setSendingFacilityID(FacilityDATIMCode);
            manifest.setSendingFacilityName(FacilityName);
            manifest.setManifestStatus("Ready");
            manifest.setCreateDate(LocalDateTime.now());
            manifest.setUuid(UUID.randomUUID().toString());
            manifest.setFacilityId(FacilityId);

            for(LIMSSample sample: manifest.getSampleInformation()){
                sample.setUuid(UUID.randomUUID().toString());
            }
        }

        return limsMapper.toManifestDto( limsManifestRepository.save(manifest));
    }

    public ManifestDTO Update(ManifestDTO manifestDTO){
        return Save(manifestDTO);
    }

    public String Delete(Integer id){
        LIMSManifest manifest = limsManifestRepository.findById(id).orElse(null);
        assert manifest != null;
        limsManifestRepository.delete(manifest);
        return id + " deleted successfully";
    }

    public ManifestDTO findById(Integer id) {
        return limsMapper.toManifestDto(limsManifestRepository.findById(id).orElse(null));
    }

    public ManifestListMetaDataDTO findAllManifests(String searchParam, int pageNo, int pageSize){
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());

        if (searchParam == null || searchParam.equals("*")) {
            Page<LIMSManifest> manifests = limsManifestRepository.findAllByFacilityId(getCurrentUserOrganization(), paging);
            return getManifestListMetaDataDto(manifests);
        } else {
            Page<LIMSManifest> manifests = limsManifestRepository
                    .findLIMSManifestByManifestIDAndFacilityId(searchParam, getCurrentUserOrganization(), paging);
            return getManifestListMetaDataDto(manifests);
        }
    }

    @Nullable
    private ManifestListMetaDataDTO getManifestListMetaDataDto(Page<LIMSManifest> manifests) {
        List<ManifestDTO> manifestDTOS = limsMapper.toManifestDtoList(manifests.getContent());

        for(ManifestDTO manifestDTO:manifestDTOS){
            List<LIMSResult> results = resultRepository.findAllByManifestRecordID(manifestDTO.getId());
            manifestDTO.setResults(results);
        }

        if (manifests.hasContent()) {
            PageDTO pageDTO = this.generatePagination(manifests);
            ManifestListMetaDataDTO manifestListMetaDataDTO = new ManifestListMetaDataDTO();
            manifestListMetaDataDTO.setTotalRecords(pageDTO.getTotalRecords());
            manifestListMetaDataDTO.setPageSize(pageDTO.getPageSize());
            manifestListMetaDataDTO.setTotalPages(pageDTO.getTotalPages());
            manifestListMetaDataDTO.setCurrentPage(pageDTO.getPageNumber());
            manifestListMetaDataDTO.setRecords(manifestDTOS);
            return manifestListMetaDataDTO;
        }

        return new ManifestListMetaDataDTO();
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

    private String GenerateManifestID(String FacilityCode){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return FacilityCode +"-"+ LocalDateTime.now().format(formatter);
    }

    private void LogInfo(String title, Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LOG.info(title+": " + objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException exception) {
            LOG.info(title+": " + exception.getMessage());
        }
    }

    public LIMSManifestResponseDTO PostManifestToServer(int id, int configId) {
        RestTemplate restTemplate = GetRestTemplate();
        HttpHeaders headers = GetHTTPHeaders();
        LIMSConfig config = limsConfigRepository.findById(configId).orElse(null);
        LogInfo("CONFIG", config);

        //Login to LIMS
        assert config != null;
        LIMSLoginResponseDTO loginResponseDTO = LoginToLIMS(restTemplate, headers, config);

        //Post request
        LIMSManifestResponseDTO response  = PostManifestRequest(restTemplate, headers, loginResponseDTO, id, config);

        //Update manifest status
        LIMSManifest dto = limsManifestRepository.findById(id).orElse(null);
        LogInfo("SUBMITTED MANIFEST", dto);
        assert dto != null;
        dto.setManifestStatus("Submitted");

        if(config.getTestFacilityDATIMCode().length()>1){
            dto.setSendingFacilityID(config.getTestFacilityDATIMCode());
            dto.setSendingFacilityName(config.getTestFacilityName());
        }

        limsManifestRepository.save(dto);

        return response;
    }

    public RestTemplate GetRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();

        //set message converters
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    private LIMSLoginResponseDTO LoginToLIMS(RestTemplate restTemplate, HttpHeaders headers, LIMSConfig config){
        LIMSLoginRequestDTO loginRequestDTO = new LIMSLoginRequestDTO();
        loginRequestDTO.setEmail(config.getConfigEmail());
        loginRequestDTO.setPassword(config.getConfigPassword());

        HttpEntity<LIMSLoginRequestDTO> loginEntity = new HttpEntity<>(loginRequestDTO, headers);
        ResponseEntity<LIMSLoginResponseDTO> loginResponse = restTemplate.exchange(config.getServerUrl()+loginUrl, HttpMethod.POST, loginEntity, LIMSLoginResponseDTO.class);
        LogInfo("LOGIN_RESPONSE", loginResponse.getBody());

        return loginResponse.getBody();
    }

    private HttpHeaders GetHTTPHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Application");
        return headers;
    }

    private LIMSManifestResponseDTO PostManifestRequest(RestTemplate restTemplate, HttpHeaders headers, LIMSLoginResponseDTO loginResponseDTO, int ManifestId, LIMSConfig config) {
        LIMSManifestDTO manifest = limsMapper.toLimsManifestDto(findById(ManifestId));

        if (config.getTestFacilityDATIMCode().length() > 1) {
            manifest.setSendingFacilityID(config.getTestFacilityDATIMCode());
            manifest.setSendingFacilityName(config.getTestFacilityName());
        }

        LIMSManifestRequestDTO requestDTO = new LIMSManifestRequestDTO();
        assert loginResponseDTO != null;
        requestDTO.setToken(loginResponseDTO.getJwt());
        requestDTO.setViralLoadManifest(manifest);
        LogInfo("MANIFEST_REQUEST", requestDTO);

        HttpEntity<LIMSManifestRequestDTO> manifestEntity = new HttpEntity<>(requestDTO, headers);
        ResponseEntity<LIMSManifestResponseDTO> manifestResponse = restTemplate.exchange(config.getServerUrl() + manifestUrl, HttpMethod.POST, manifestEntity, LIMSManifestResponseDTO.class);
        LogInfo("MANIFEST_RESPONSE", manifestResponse.getBody());

        return manifestResponse.getBody();
    }

    private LIMSResultsResponseDTO GetResultsRequest(RestTemplate restTemplate, HttpHeaders headers, LIMSLoginResponseDTO loginResponseDTO, int ManifestId, LIMSConfig config){
        LIMSManifestDTO manifest = limsMapper.toLimsManifestDto(findById(ManifestId));
        LIMSResultsRequestDTO requestDTO = new LIMSResultsRequestDTO();

        requestDTO.setToken(loginResponseDTO.getJwt());
        requestDTO.setManifestID(manifest.getManifestID());
        requestDTO.setReceivingPCRLabID(manifest.getReceivingLabID());
        requestDTO.setReceivingPCRLabName(manifest.getReceivingLabName());
        requestDTO.setTestType("VL");
        requestDTO.setSendingFacilityID(manifest.getSendingFacilityID());
        requestDTO.setSendingFacilityName(manifest.getSendingFacilityName());
        LogInfo("RESULTS_REQUEST", requestDTO);

        HttpEntity<LIMSResultsRequestDTO> manifestEntity = new HttpEntity<>(requestDTO, headers);
        ResponseEntity<LIMSResultsResponseDTO> manifestResponse = restTemplate.exchange(config.getServerUrl()+resultsUrl, HttpMethod.POST, manifestEntity, LIMSResultsResponseDTO.class);
        LogInfo("RESULTS_RESPONSE", manifestResponse.getBody());

        return manifestResponse.getBody();
    }

    public LIMSResultsResponseDTO DownloadResultsFromLIMS(int id, int configId) {
        RestTemplate restTemplate = GetRestTemplate();
        HttpHeaders headers = GetHTTPHeaders();
        LIMSConfig config = limsConfigRepository.findById(configId).orElse(null);

        //Login to LIMS
        assert config != null;
        LIMSLoginResponseDTO loginResponseDTO = LoginToLIMS(restTemplate, headers, config);

        //Get results
        LIMSResultsResponseDTO response  = GetResultsRequest(restTemplate, headers, loginResponseDTO, id, config);
        LOG.info("RESPONSE:"+response);

        try {
            for (LIMSResultDTO result : response.getViralLoadTestReport()) {
                LOG.info("RESULT: " + result);
                result.setManifestRecordID(id);
                resultService.Save(limsMapper.toResult(result));
            }
        }catch (Exception e) {
            LOG.error("ERROR:" + e);
        }

        return response;
    }

    public Long getCurrentUserOrganization() {
        Optional<User> userWithRoles = userService.getUserWithRoles ();
        return userWithRoles.map (User::getCurrentOrganisationUnitId).orElse (null);
    }

    public PersonMetaDataDto findAllManifestsV2(String searchParam, int pageNo, int pageSize)
    {
        ArrayList<AllManifestDto> allManifestDtoArrayList = new ArrayList<>();
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        List<LIMSManifest> limsManifestList = this.limsManifestRepository.findAllByFacilityId(getCurrentUserOrganization(), paging).getContent();
        Iterator it1 = limsManifestList.listIterator();
        while(it1.hasNext())
        {
            LIMSManifest limsManifest = (LIMSManifest) it1.next();
            int manifestId = limsManifest.getId();
            List<LIMSSample> limsSampleList = this.sampleRepository.findAllByManifestRecordID(manifestId);
            Iterator it2 = limsSampleList.listIterator();
            while(it2.hasNext())
            {
                LIMSSample limsSample = (LIMSSample)   it2.next();
                AllManifestDto allManifestDto = new AllManifestDto();
                allManifestDto.setLocalManifestId(limsManifest.getId());
                allManifestDto.setManifestID(limsManifest.getManifestID());
                allManifestDto.setSendingFacilityID(limsManifest.getSendingFacilityID());
                allManifestDto.setSendingFacilityName(limsManifest.getSendingFacilityName());
                allManifestDto.setReceivingLabID(limsManifest.getReceivingLabID());
                allManifestDto.setReceivingLabName(limsManifest.getReceivingLabName());
                allManifestDto.setDateScheduledForPickup(limsManifest.getDateScheduledForPickup());
                allManifestDto.setTemperatureAtPickup(limsManifest.getTemperatureAtPickup());
                allManifestDto.setSamplePackagedBy(limsManifest.getSamplePackagedBy());
                allManifestDto.setCourierRiderName(limsManifest.getCourierRiderName());
                allManifestDto.setCourierContact(limsManifest.getCourierContact());
                allManifestDto.setManifestStatus(limsManifest.getManifestStatus());
                allManifestDto.setCreateDate(limsManifest.getCreateDate());
                allManifestDto.setFacilityId(limsManifest.getFacilityId());

                allManifestDto.setLocalSampleId(limsSample.getId());
                allManifestDto.setPatientID(limsSample.getPatientID());
                allManifestDto.setPid(limsSample.getPid());
                allManifestDto.setSampleID(limsSample.getSampleID());
                allManifestDto.setSampleType(limsSample.getSampleType());
                allManifestDto.setSampleOrderedBy(limsSample.getSampleOrderedBy());
                allManifestDto.setSampleOrderDate(limsSample.getSampleOrderDate());
                allManifestDto.setSampleCollectedBy(limsSample.getSampleCollectedBy());
                allManifestDto.setSampleCollectionDate(limsSample.getSampleCollectionDate());
                allManifestDto.setSampleCollectionTime(limsSample.getSampleCollectionTime());
                allManifestDto.setDateSampleSent(limsSample.getDateSampleSent());
                allManifestDto.setIndicationVLTest(limsSample.getIndicationVLTest());
                allManifestDto.setFirstName(limsSample.getFirstName());
                allManifestDto.setSurName(limsSample.getSurName());
                allManifestDto.setSex(limsSample.getSex());
                allManifestDto.setAge(limsSample.getAge());
                allManifestDto.setDateOfBirth(limsSample.getDateOfBirth());
                allManifestDto.setPregnantBreastFeedingStatus(limsSample.getPregnantBreastFeedingStatus());
                allManifestDto.setArtCommencementDate(limsSample.getArtCommencementDate());
                allManifestDto.setPriority(limsSample.getPriority());
                allManifestDto.setPriorityReason(limsSample.getPriorityReason());

                Optional<LIMSResult> limsResults = this.resultRepository.getLIMSResultBySampleID(limsSample.getSampleID());
                if (limsResults.isPresent()) {
                    allManifestDto.setResultIsBack(Boolean.TRUE);
                    LIMSResult limsResult = limsResults.get();
                    allManifestDto.setLocalResultId(limsResult.getId());
                    allManifestDto.setPcrLabSampleNumber(limsResult.getPcrLabSampleNumber());
                    allManifestDto.setVisitDate(limsResult.getVisitDate());
                    allManifestDto.setDateSampleReceivedAtPcrLab(limsResult.getDateSampleReceivedAtPcrLab());
                    allManifestDto.setResultDate(limsResult.getResultDate());
                    allManifestDto.setTestResult(limsResult.getTestResult());
                    allManifestDto.setAssayDate(limsResult.getAssayDate());
                    allManifestDto.setApprovalDate(limsResult.getApprovalDate());
                    allManifestDto.setDateResultDispatched(limsResult.getDateResultDispatched());
                    allManifestDto.setSampleStatus(limsResult.getSampleStatus());
                    allManifestDto.setSampleTestable(limsResult.getSampleTestable());

                    allManifestDto.setTransferStatus(limsResult.getTransferStatus());
                    allManifestDto.setTestedBy(limsResult.getTestedBy());
                    allManifestDto.setApprovedBy(limsResult.getApprovedBy());
                    allManifestDto.setDateTransferredOut(limsResult.getDate_Transferred_Out());
                    allManifestDto.setReasonNotTested(limsResult.getReasonNotTested());
                    allManifestDto.setOtherRejectionReason(limsResult.getOtherRejectionReason());
                    allManifestDto.setSendingPcrLabID(limsResult.getSendingPcrLabID());
                    allManifestDto.setSendingPcrLabName(limsResult.getSendingPcrLabName());


                }else allManifestDto.setResultIsBack(Boolean.FALSE);

                allManifestDtoArrayList.add(allManifestDto);

            }


        }
        PersonMetaDataDto personMetaDataDto = new PersonMetaDataDto();
        personMetaDataDto.setTotalRecords(allManifestDtoArrayList.size());
        personMetaDataDto.setPageSize(pageSize);
        personMetaDataDto.setTotalPages(getTotalPages(allManifestDtoArrayList.size(),pageSize ));
        personMetaDataDto.setCurrentPage(pageNo);
        personMetaDataDto.setRecords(allManifestDtoArrayList);
        return  personMetaDataDto;

    }
     public int getTotalPages(int totalRec, int pageSize)
     {
         return (int)Math.ceil(totalRec/pageSize);
     }

    public  AllManifestDto getSingleSampleInformationBySampleId(String sampleId){
        AllManifestDto allManifestDto = new AllManifestDto();
        Optional<LIMSSample>  limsSamples = sampleRepository.findLIMSSampleBySampleID(sampleId);
        if(limsSamples.isPresent())
        {
            LIMSSample limsSample = limsSamples.get();
            Optional<LIMSManifest> limsManifests = limsManifestRepository.findById(limsSample.getManifestRecordID());
            if (limsManifests.isPresent()){
                LIMSManifest limsManifest = limsManifests.get();
                allManifestDto.setLocalManifestId(limsManifest.getId());
                allManifestDto.setManifestID(limsManifest.getManifestID());
                allManifestDto.setSendingFacilityID(limsManifest.getSendingFacilityID());
                allManifestDto.setSendingFacilityName(limsManifest.getSendingFacilityName());
                allManifestDto.setReceivingLabID(limsManifest.getReceivingLabID());
                allManifestDto.setReceivingLabName(limsManifest.getReceivingLabName());
                allManifestDto.setDateScheduledForPickup(limsManifest.getDateScheduledForPickup());
                allManifestDto.setTemperatureAtPickup(limsManifest.getTemperatureAtPickup());
                allManifestDto.setSamplePackagedBy(limsManifest.getSamplePackagedBy());
                allManifestDto.setCourierRiderName(limsManifest.getCourierRiderName());
                allManifestDto.setCourierContact(limsManifest.getCourierContact());
                allManifestDto.setManifestStatus(limsManifest.getManifestStatus());
                allManifestDto.setCreateDate(limsManifest.getCreateDate());
                allManifestDto.setFacilityId(limsManifest.getFacilityId());
            }

            allManifestDto.setLocalSampleId(limsSample.getId());
            allManifestDto.setPatientID(limsSample.getPatientID());
            allManifestDto.setPid(limsSample.getPid());
            allManifestDto.setSampleID(limsSample.getSampleID());
            allManifestDto.setSampleType(limsSample.getSampleType());
            allManifestDto.setSampleOrderedBy(limsSample.getSampleOrderedBy());
            allManifestDto.setSampleOrderDate(limsSample.getSampleOrderDate());
            allManifestDto.setSampleCollectedBy(limsSample.getSampleCollectedBy());
            allManifestDto.setSampleCollectionDate(limsSample.getSampleCollectionDate());
            allManifestDto.setSampleCollectionTime(limsSample.getSampleCollectionTime());
            allManifestDto.setDateSampleSent(limsSample.getDateSampleSent());
            allManifestDto.setIndicationVLTest(limsSample.getIndicationVLTest());
            allManifestDto.setFirstName(limsSample.getFirstName());
            allManifestDto.setSurName(limsSample.getSurName());
            allManifestDto.setSex(limsSample.getSex());
            allManifestDto.setAge(limsSample.getAge());
            allManifestDto.setDateOfBirth(limsSample.getDateOfBirth());
            allManifestDto.setPregnantBreastFeedingStatus(limsSample.getPregnantBreastFeedingStatus());
            allManifestDto.setArtCommencementDate(limsSample.getArtCommencementDate());
            allManifestDto.setPriority(limsSample.getPriority());
            allManifestDto.setPriorityReason(limsSample.getPriorityReason());

            Optional<LIMSResult> limsResults = this.resultRepository.getLIMSResultBySampleID(limsSample.getSampleID());
            if (limsResults.isPresent()) {
                allManifestDto.setResultIsBack(Boolean.TRUE);
                LIMSResult limsResult = limsResults.get();
                allManifestDto.setLocalResultId(limsResult.getId());
                allManifestDto.setPcrLabSampleNumber(limsResult.getPcrLabSampleNumber());
                allManifestDto.setVisitDate(limsResult.getVisitDate());
                allManifestDto.setDateSampleReceivedAtPcrLab(limsResult.getDateSampleReceivedAtPcrLab());
                allManifestDto.setResultDate(limsResult.getResultDate());
                allManifestDto.setTestResult(limsResult.getTestResult());
                allManifestDto.setAssayDate(limsResult.getAssayDate());
                allManifestDto.setApprovalDate(limsResult.getApprovalDate());
                allManifestDto.setDateResultDispatched(limsResult.getDateResultDispatched());
                allManifestDto.setSampleStatus(limsResult.getSampleStatus());
                allManifestDto.setSampleTestable(limsResult.getSampleTestable());

                allManifestDto.setTransferStatus(limsResult.getTransferStatus());
                allManifestDto.setTestedBy(limsResult.getTestedBy());
                allManifestDto.setApprovedBy(limsResult.getApprovedBy());
                allManifestDto.setDateTransferredOut(limsResult.getDate_Transferred_Out());
                allManifestDto.setReasonNotTested(limsResult.getReasonNotTested());
                allManifestDto.setOtherRejectionReason(limsResult.getOtherRejectionReason());
                allManifestDto.setSendingPcrLabID(limsResult.getSendingPcrLabID());
                allManifestDto.setSendingPcrLabName(limsResult.getSendingPcrLabName());

            }else allManifestDto.setResultIsBack(Boolean.FALSE);

        }

        return allManifestDto;
    }
}
