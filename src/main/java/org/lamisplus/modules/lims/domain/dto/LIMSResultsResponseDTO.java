package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class LIMSResultsResponseDTO {
    String manifestID;
    String receivingFacilityID;
    String receivingFacilityName;
    String sendingPCRLabID;
    String sendingPCRLabName;
    String testType;
    List<LIMSResultDTO> viralLoadTestReport;
}
