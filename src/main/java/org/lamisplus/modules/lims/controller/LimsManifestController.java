package org.lamisplus.modules.lims.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.service.LimsManifestService;
import org.lamisplus.modules.patient.domain.dto.PersonMetaDataDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lims")
public class LimsManifestController {
    private final LimsManifestService limsManifestService;

    @PostMapping("/manifests")
    public ManifestDTO SaveManifest(@RequestBody ManifestDTO manifestDTO){
        return limsManifestService.Save(manifestDTO);
    }

    @PutMapping("/manifests")
    public ManifestDTO UpdateManifest(@RequestBody ManifestDTO manifestDTO){
        return limsManifestService.Update(manifestDTO);
    }

    @DeleteMapping("/manifests/{id}")
    public String DeleteManifest(@PathVariable int id){
        return limsManifestService.Delete(id);
    }

    @GetMapping("/manifests/{id}")
    public ManifestDTO GetManifestById(@PathVariable int id){
        return limsManifestService.findById(id);
    }

    @GetMapping("/manifests")
    public ManifestListMetaDataDTO GetAllManifests(@RequestParam(defaultValue = "*") String searchParam,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize){
        return limsManifestService.findAllManifests(searchParam, pageNo, pageSize);
    }

    @GetMapping("/ready-manifests/{id}/{configId}")
    public LIMSManifestResponseDTO PostManifestsToLIMSServer(@PathVariable int id, @PathVariable int configId) {
        return limsManifestService.PostManifestToServer(id, configId);
    }

    @GetMapping("/manifest-results/{id}/{configId}")
    public LIMSResultsResponseDTO DownloadResults(@PathVariable int id, @PathVariable int configId) {
        return limsManifestService.DownloadResultsFromLIMS(id, configId);
    }

    @GetMapping("/manifests-all-in-one-drkarim")
    public PersonMetaDataDto getAllManifests2(@RequestParam(defaultValue = "*") String searchParam,
                                              @RequestParam(defaultValue = "0") Integer pageNo,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return limsManifestService.findAllManifestsV2(searchParam, pageNo, pageSize);
    }

    @GetMapping("/manifest-samples-info-by-sampleid/{sampleId}")
    public AllManifestDto getSamplesInformationBySampleId(@PathVariable String sampleId) {
        return limsManifestService.getSingleSampleInformationBySampleId(sampleId);
    }
}
