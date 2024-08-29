package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

@Data
public class LIMSManifestResponseErrorDTO {
    private String sampleId;
    private String reasons;
}
