package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

@Data
public class LIMSManifestRequestDTO {
    String token;
    LIMSManifestDTO viralLoadManifest;
}
