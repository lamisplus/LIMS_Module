package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
public class ConfigDTO {
    private Integer id;
    private String configName;
    private String configEmail;
    private String configPassword;
    private String serverUrl;
    private String testFacilityName;
    private String testFacilityDATIMCode;
}
