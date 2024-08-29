package org.lamisplus.modules.lims.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LIMSManifestDTO {
    private String manifestID;
    private String sendingFacilityID;
    private String sendingFacilityName;
    private String receivingLabID;
    private String receivingLabName;
    private String dateScheduledForPickup;
    private String temperatureAtPickup;
    private String samplePackagedBy;
    private String courierRiderName;
    private String courierContact;
    private String manifestStatus;
    private LocalDateTime createDate;
    private List<LIMSSampleDTO> sampleInformation;
}