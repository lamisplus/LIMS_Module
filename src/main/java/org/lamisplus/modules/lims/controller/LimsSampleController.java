package org.lamisplus.modules.lims.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.LABSampleDTO;
import org.lamisplus.modules.lims.domain.dto.LABSampleMetaDataDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.lamisplus.modules.lims.service.LimsSampleService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lims")
public class LimsSampleController {
    private final LimsSampleService limsSampleService;

    @PostMapping("/manifest-samples")
    public LABSampleDTO SaveManifest(@RequestBody LABSampleDTO manifestDTO) {
        return limsSampleService.Save(manifestDTO);
    }

    @PutMapping("/manifest-samples")
    public LABSampleDTO UpdateManifest(@RequestBody LABSampleDTO manifestDTO) {
        return limsSampleService.Update(manifestDTO);
    }

    @DeleteMapping("/manifest-samples/{id}")
    public String DeleteManifest(@PathVariable int id) {
        return limsSampleService.Delete(id);
    }

    @GetMapping("/manifest-samples/{id}")
    public LABSampleDTO GetAllLabOrders(@PathVariable int id) {
        return limsSampleService.findById(id);
    }

    @GetMapping("/manifest-samples/manifests/{id}")
    public List<LABSampleDTO> GetSamplesByManifestId(@PathVariable int id) {
        return limsSampleService.findbyManifestRecordId(id);
    }

//    public List<LabSampleProjection> getPendingLabSamples(LocalDate from, LocalDate to, int pageNo, int pageSize ) {
//        int offset = (pageNo - 1) * pageSize;
//        return  labSampleRepositoryCustom.findLabSamples(getCurrentUserOrganization(),  from,  to,  offset,  pageSize );
//    }


    @GetMapping("/collected-samples/")
    public LABSampleMetaDataDTO GetAllCollectedSamples(@RequestParam(defaultValue = "*") String searchParam,
                                                       @RequestParam(defaultValue = "0") Integer pageNo,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        // need to be revisited
        return limsSampleService.getAllPendingSamples(searchParam, pageNo, pageSize);
    }

    @GetMapping("/lab-samples/pending")
    public Page<LABSampleDTO> getPendingLabSamplesByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return limsSampleService.getPendingLabSamples(startDate, endDate, pageNo, pageSize);
    }
}
