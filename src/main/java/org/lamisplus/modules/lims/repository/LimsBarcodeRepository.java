package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LimsBarcodeRepository extends JpaRepository<LIMSBarcode, Integer> {
    @Query(value="SELECT * FROM lims_barcode WHERE manifest_id = ?1 order by id desc", nativeQuery = true)
    List<LIMSBarcode> findLIMSBarcodesByManifestID(String manifestId);

    @Query(value="SELECT * FROM lims_barcode WHERE manifest_id = ?1 order by id desc LIMIT 1", nativeQuery = true)
    LIMSBarcode checkBarcodesByManifestID(String manifestId);
}
