package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

@Data
public class LIMSResultsRequestDTO {
    String token;
    String manifestID;
    String sendingFacilityID;
    String sendingFacilityName;
    String testType;
    String receivingPCRLabID;
    String receivingPCRLabName;
}
