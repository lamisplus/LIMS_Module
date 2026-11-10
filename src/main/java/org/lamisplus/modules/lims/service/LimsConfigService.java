package org.lamisplus.modules.lims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.lims.domain.dto.ConfigDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSConfig;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LimsConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LimsConfigService {


    private final LimsConfigRepository limsConfigRepository;
    private final LimsMapper limsMapper;

    @Transactional
    public ConfigDTO save(ConfigDTO configDTO) {
        List<LIMSConfig> configs = limsConfigRepository.findAll();

        if (!configs.isEmpty()) {
            Integer existingId = configs.get(0).getId();
            return update(configDTO, existingId);
        }

        LIMSConfig config = limsMapper.toConfig(configDTO);
        config.setUuid(UUID.randomUUID().toString());
        config.setCreateDate(LocalDateTime.now());

        LIMSConfig savedConfig = limsConfigRepository.save(config);

        return limsMapper.toConfigDto(savedConfig);
    }
    @Transactional
    public ConfigDTO update(ConfigDTO configDTO, Integer id) {
        LIMSConfig existingConfig = limsConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("LIMS configuration with ID " + id + " not found."));


        LIMSConfig configToUpdate = limsMapper.toConfig(configDTO);

        configToUpdate.setId(existingConfig.getId());
        configToUpdate.setUuid(existingConfig.getUuid());
        configToUpdate.setCreateDate(existingConfig.getCreateDate());


        LIMSConfig updatedConfig = limsConfigRepository.save(configToUpdate);

        return limsMapper.toConfigDto(updatedConfig);
    }

    public ConfigDTO findById(Integer id) {
        return limsConfigRepository.findById(id)
                .map(limsMapper::toConfigDto)
                .orElseThrow(() -> new EntityNotFoundException("LIMS configuration with ID " + id + " not found."));
    }

    @Transactional
    public String delete(Integer id) {
        if (!limsConfigRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot delete. LIMS configuration with ID " + id + " not found.");
        }
        limsConfigRepository.deleteById(id);
        return id + " deleted successfully";
    }

    public List<ConfigDTO> findAll() {
        return limsMapper.toConfigDtoList(limsConfigRepository.findAll());
    }
}




