package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LimsTestRepository extends JpaRepository<LIMSTest, Integer> {
    @Query(value="select * from laboratory_test where id= " +
            "(select test_id from laboratory_sample where sample_number=:sampleId order by date_created desc limit 1)", nativeQuery = true)
    List<LIMSTest> findBySampleId(@Param("sampleId") String sampleId);

    @Query(value="select * from laboratory_test where id= " +
            "(select test_id from laboratory_sample where sample_number=:sampleId  and patient_uuid =:personUuid and archived = 0  order by date_sample_collected desc limit 1)", nativeQuery = true)
    Optional<LIMSTest> findBySampleIdAndPersonUuid(@Param("sampleId") String sampleId, @Param("personUuid") String personUuid);

    @Query(value = "SELECT uuid FROM patient_person WHERE hospital_number=:hospitalNum ", nativeQuery = true)
    Optional<String>  getPersonUuidByHospitalNum(@Param("hospitalNum") String hospitalNum);

    @Query(value="select * from laboratory_test where id=:testId", nativeQuery = true)
    LIMSTest findByTestId(@Param("testId") Integer testId);

    @Query(value="select count(test_id) > 0 from laboratory_result where test_id=:testId", nativeQuery = true)
    boolean findResultByTestId(@Param("testId") Integer testId);
}
