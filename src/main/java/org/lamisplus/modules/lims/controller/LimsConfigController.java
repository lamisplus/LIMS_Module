package org.lamisplus.modules.lims.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.ConfigDTO;
import org.lamisplus.modules.lims.service.LimsConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lims")
public class LimsConfigController {
    private final LimsConfigService limsConfigService;

    @PostMapping("/config")
    public ConfigDTO SaveConfig(@RequestBody ConfigDTO configDTO) throws Exception {
        return limsConfigService.Save(configDTO);
    }

    @PutMapping("/config/{id}")
    public ConfigDTO UpdateConfig(@PathVariable int id, @RequestBody ConfigDTO configDTO) throws Exception {
        return limsConfigService.Update(configDTO, id);
    }

    @DeleteMapping("/config/{id}")
    public String DeleteConfig(@PathVariable int id) throws Exception {
        return limsConfigService.Delete(id);
    }

    @GetMapping("/config")
    public ConfigDTO GetConfig(){
        boolean config = limsConfigService.FindAll().isEmpty();
        if (config != true) {
            return limsConfigService.FindAll().get(0);
        }
        return null;
    }

    @GetMapping("/config/{id}")
    public ConfigDTO GetConfigById(@PathVariable int id){
        return limsConfigService.FindById(id);
    }
}
