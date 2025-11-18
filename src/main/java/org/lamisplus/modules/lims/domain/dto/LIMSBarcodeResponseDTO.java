package org.lamisplus.modules.lims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LIMSBarcodeResponseDTO {
    private String serialNumber;
    private String barcodeImage;
    private String sampleId;
}
