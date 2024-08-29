package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class LIMSManifestResponseDTO {
    String manifestID;
    String facilityName;
    String facilityId;
    String receivingPCRLab;
    String receivingPCRLabId;
    String totalSamplesProcessed;
    String totalSamplesNotProcessed;
//    @JsonProperty("errors")
//    private JsonNode errors;
    private List<LIMSManifestResponseErrorDTO> errors;
}
