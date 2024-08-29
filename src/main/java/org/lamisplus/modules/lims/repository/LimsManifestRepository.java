package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LimsManifestRepository extends JpaRepository<LIMSManifest, Integer> {
    Page<LIMSManifest> findLIMSManifestByManifestIDAndFacilityId(String manifestID, Long facilityId, Pageable pageable);
    Page<LIMSManifest> findAllByFacilityId(Long facilityId, Pageable pageable);

    Optional<LIMSManifest> findLIMSManifestByManifestID (Integer integer);
}
