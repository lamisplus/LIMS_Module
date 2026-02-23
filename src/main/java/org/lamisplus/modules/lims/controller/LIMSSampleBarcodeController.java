package org.lamisplus.modules.lims.controller;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.lims.domain.dto.ApiResponse;
import org.lamisplus.modules.lims.domain.dto.LIMSBarcodeResponseDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSBarcode;
import org.lamisplus.modules.lims.service.LIMSBarcodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;
import java.util.List;

@RestController
@RequestMapping("/api/v1/barcodes")
@RequiredArgsConstructor
public class LIMSSampleBarcodeController {
    private final LIMSBarcodeService limsBarcodeService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateBarcode(int configId, int manifestId, int count, String user) {
        try {
            List<String> serialNumbers = limsBarcodeService.getSerialNumbers(configId, manifestId, count, user);

            List<LIMSBarcodeResponseDTO> results = limsBarcodeService.generateBarcodes(serialNumbers, manifestId, user);

            if (results != null) {
                return ResponseEntity.ok(ApiResponse.success(results));
            }else{
                return ResponseEntity.status(201).body(ApiResponse.error("Barcode previously generated for this manifest. "));
            }
        }catch(RuntimeException ex){
            return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage()));
        }catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.error("Internal Server Error " + ex.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getGeneratedBarcodes(int manifestId) {
        try {
            List<LIMSBarcode> barcodes = limsBarcodeService.getManifestBarcodes(manifestId);
            return ResponseEntity.ok(ApiResponse.success(barcodes));
        }catch(RuntimeException ex){
            return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage()));
        }catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.error("Internal Server Error " + ex.getMessage()));
        }
    }
}
