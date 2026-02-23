package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

import java.util.List;
@Data
public class LIMSSerialCodeResponseDTO {
    private String status;
    private List<String> serialNumbers;
}
