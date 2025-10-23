package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.dto.LIMSSampleProjection;
import org.lamisplus.modules.lims.domain.entity.LIMSManifest;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
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

    @Query(value="SELECT id, manifest_record_id AS \"manifestRecordID\", sample_id AS \"sampleID\", test_id AS \"testID\",\n" +
            "CAST(uuid AS text) AS \"uuid\", date_of_birth AS \"dateOfBirth\", pid, CAST(patient_id AS text) AS \"patientID\", sample_type AS \"sampleType\",\n" +
            "sample_ordered_by AS \"sampleOrderedBy\", sample_order_date AS \"sampleOrderDate\", sample_collected_by AS \"sampleCollectedBy\",\n" +
            "sample_collection_date AS \"sampleCollectionDate\", sample_collection_time AS \"sampleCollectionTime\", \n" +
            "date_sample_sent AS \"dateSampleSent\", indication_vl_test AS \"indicationVLTest\", first_name AS \"firstName\", \n" +
            "surname AS \"surName\", sex, age, hospital_number AS \"hospitalNumber\", drug_regimen AS \"drugRegimen\",\n" +
            "sending_facility_id AS \"sendingFacilityID\", sending_facility_name AS \"sendingFacilityName\", priority, \n" +
            "priority_reason AS \"priorityReason\", unique_id AS \"uniqueId\" FROM lims_sample WHERE sample_id  = ?1 AND test_id  = ?2", nativeQuery = true)
    Optional<LIMSSampleProjection> findLIMSSampleBySampleID(String sampleId, Integer testId);

    @Query(value="SELECT id  FROM lims_manifest WHERE manifest_id  = ?1", nativeQuery = true)
    List<Integer> getManifestId(String manifiestId);
}
