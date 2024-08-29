package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

@Data
public class LIMSLoginRequestDTO {
    private String email;
    private String password;
}
