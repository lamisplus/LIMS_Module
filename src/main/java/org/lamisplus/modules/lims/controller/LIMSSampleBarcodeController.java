package org.lamisplus.modules.lims.controller;

import org.lamisplus.modules.lims.domain.dto.ApiResponse;
import org.lamisplus.modules.lims.domain.dto.LIMSBarcodeResponseDTO;
import org.lamisplus.modules.lims.service.LIMSBarcodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;
import java.util.List;

@RestController
@RequestMapping("/api/v1/barcodes")
public class LIMSSampleBarcodeController {
    private final LIMSBarcodeService limsBarcodeService;

    public LIMSSampleBarcodeController(LIMSBarcodeService limsBarcodeService) {
        this.limsBarcodeService = limsBarcodeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateBarcode(int configId, int manifestId, int count) {
        try {
            List<String> serialNumbers = limsBarcodeService.getSerialNumbers(configId, manifestId, count);

            List<LIMSBarcodeResponseDTO> results = limsBarcodeService.generateBarcodes(serialNumbers);

            return ResponseEntity.ok(ApiResponse.success(results));

        }catch(RuntimeException ex){
            return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage()));
        }catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.error("Internal Server Error " + ex.getMessage()));
        }
    }
}
