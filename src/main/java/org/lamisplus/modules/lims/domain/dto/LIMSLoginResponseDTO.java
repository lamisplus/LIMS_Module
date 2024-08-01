package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

@Data
public class LIMSLoginResponseDTO {
    private String message;
    private String jwt;
}
