package org.lamisplus.modules.lims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.ConfigDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSConfig;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LimsConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LimsConfigService {
    private final LimsConfigRepository limsConfigRepository;
    private final LimsMapper limsMapper;

    public ConfigDTO Save(ConfigDTO configDTO) throws Exception {
        List<LIMSConfig> configs = limsConfigRepository.findAll();
        LIMSConfig config = limsMapper.toConfig(configDTO);
        config.setUuid(UUID.randomUUID().toString());
        config.setCreateDate(LocalDateTime.now());

        if(configs.size()>0){
            configDTO.setId(configs.get(0).getId());
            return Update(configDTO, config.getId());
        }
        else{
            return limsMapper.toConfigDto(limsConfigRepository.save(config));
        }
    }
    public ConfigDTO Update(ConfigDTO configDTO, int id) throws Exception {
        if(FindById(id) != null) {
            return limsMapper.toConfigDto(limsConfigRepository.save(limsMapper.toConfig(configDTO)));
        }
        else {
            throw new Exception(id + " not found");
        }
    }

    public ConfigDTO FindById(Integer id){
        return limsMapper.toConfigDto(limsConfigRepository.findById(id).orElse(null));
    }
    public String Delete(Integer id){
        limsConfigRepository.deleteById(id);
        return id+" deleted successfully";
    }
    public List<ConfigDTO> FindAll(){
        return limsMapper.toConfigDtoList(limsConfigRepository.findAll());
    }
}
