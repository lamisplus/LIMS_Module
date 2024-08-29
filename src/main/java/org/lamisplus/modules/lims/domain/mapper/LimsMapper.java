package org.lamisplus.modules.lims.domain.mapper;

import org.lamisplus.modules.lims.domain.dto.*;
import org.lamisplus.modules.lims.domain.entity.LIMSConfig;
import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.lamisplus.modules.lims.domain.entity.LIMSResult;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LimsMapper {
    ManifestDTO toManifestDto(LIMSManifest manifest);
    LABSampleDTO tosSampleDto(LIMSSample sample);
    List<ManifestDTO> toManifestDtoList(List<LIMSManifest> manifestList);
    List<LABSampleDTO> toSampleDtoList(List<LIMSSample> sampleList);

    LIMSManifest tomManifest(ManifestDTO manifestDTO);
    LIMSSample toSample(LABSampleDTO sampleDTO);
    List<LIMSManifest> tomManifestList(List<ManifestDTO> manifestDTOList);
    List<LIMSSample> toSampleList(List<LABSampleDTO> sampleDTOList);

    LIMSManifestDTO toLimsManifestDto(ManifestDTO manifestDTO);
    LIMSSampleDTO toLimsSampleDto(LABSampleDTO sampleDTO);
    ManifestDTO toManifestDto(LIMSManifestDTO limsManifestDTO);
    LABSampleDTO toSampleDto(LIMSSampleDTO limsSampleDTO);

    ConfigDTO toConfigDto(LIMSConfig config);
    LIMSConfig toConfig(ConfigDTO configDTO);
    List<ConfigDTO> toConfigDtoList(List<LIMSConfig> configList);
    List<LIMSConfig> toConfigList(List<ConfigDTO> configDTOList);

    LIMSResult toResult(LIMSResultDTO resultDTO);
}
