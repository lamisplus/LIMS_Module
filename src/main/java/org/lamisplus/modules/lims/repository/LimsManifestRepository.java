package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface
LimsManifestRepository extends JpaRepository<LIMSManifest, Integer> {
    Page<LIMSManifest> findLIMSManifestByManifestIDAndFacilityId(String manifestID, Long facilityId, Pageable pageable);
    Page<LIMSManifest> findAllByFacilityId(Long facilityId, Pageable pageable);

    @Query(value="SELECT * FROM lims_manifest WHERE id  = ?1", nativeQuery = true)
    Optional<LIMSManifest> findLIMSManifestByManifestID (Integer id);

    @Query(value="SELECT id  FROM lims_manifest WHERE manifest_id  = ?1", nativeQuery = true)
    List<Integer> getManifestId(String manifiestId);
}
