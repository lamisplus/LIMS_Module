package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.dto.LABSampleDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LimsSampleRepository extends JpaRepository<LIMSSample, Integer> {
    List<LIMSSample> findAllByManifestRecordID(Integer id);
    //List<LIMSSample> findAllByManifestRecordIDAndSendingFacilityID(Integer id, S)
    @Query(value = "select c.id, '0' as manifest_record_id\n" +
            ", c.sample_number as sample_id\n" +
            ", c.uuid\n" +
            ", a.patient_id as pid\n" +
            ", null as patient_id\n" +
            ", e.sample_type_name as sample_type\n" +
            ", a.userid as sample_ordered_by\n" +
            ", CAST(a.order_date as TEXT) as sample_order_date\n" +
            ", c.sample_collected_by\n" +
            ", CAST(c.date_sample_collected as TEXT) as sample_collection_date\n" +
            ", '00:00:00' as sample_collection_time\n" +
            ", '' as date_sample_sent\n" +
            ", f.display as indication_vl_test\n" +
            ", '' as first_name\n" +
            ", '' as surname\n" +
            ", '' as sex\n" +
            ", '' as age\n" +
            ", '' as date_of_birth\n" +
            ", '' as pregnant_breast_feeding_status\n" +
            ", '' as art_commencement_date\n" +
            ", '' as drug_regimen\n" +
            ", '' as sending_facility_id\n" +
            ", '' as sending_facility_name\n" +
            ", '' as priority\n" +
            ", '' as priority_reason\n" +
            "from laboratory_order a\n" +
            "inner join laboratory_test b on a.id=b.lab_order_id\n" +
            "inner join laboratory_sample c on b.id=c.test_id\n" +
            "inner join laboratory_labtest d on b.lab_test_id=d.id\n" +
            "inner join laboratory_sample_type e on c.sample_type_id = e.id\n" +
            "left join base_application_codeset f on b.viral_load_indication = e.id\n" +
            "where d.lab_test_name='Viral Load' and a.facility_id=?1\n" +
            "and b.lab_test_order_status in (1,2,3)\n" +
            "and c.sample_number not in (select x.sample_id from lims_sample x) ", nativeQuery = true)
    Page<LIMSSample> findPendingVLSamples(Long facilityId, Pageable pageable);

    Optional<LIMSSample> findLIMSSampleBySampleID(String sampleId);
}
