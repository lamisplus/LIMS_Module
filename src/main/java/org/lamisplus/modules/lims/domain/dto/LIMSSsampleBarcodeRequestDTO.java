package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class LIMSSsampleBarcodeRequestDTO {
    private String token;
    private String receivingLabID;
    private String receivingLabName;
    private String sendingFacilityID;
    private String sendingFacilityName;
    private String testType;
    private String sessionId;
    @Min(value = 1, message = "count must be as least 1")
    @Max(value = 50, message = "Count cannot exceed 100 per request")
    private int count;
}
