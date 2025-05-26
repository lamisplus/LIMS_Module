package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LimsResultRepository extends JpaRepository<LIMSResult, Integer> {
    List<LIMSResult> findAllByManifestRecordID(Integer id);

    List<LIMSResult> findAllBySampleID(String sampleId);

    @Query(value="SELECT * FROM lims_result WHERE sample_id = ?1", nativeQuery = true)
    Optional<LIMSResult> getLIMSResultBySampleID(String sampleId);

    @Query(value="SELECT * FROM lims_result WHERE manifest_record_id = ?1", nativeQuery = true)
    List<LIMSResult> getLIMSResultByManifestId(Integer  manifestId);

    @Query(value="SELECT * FROM lims_result WHERE manifest_record_id = ?1 AND sample_id =  ?2", nativeQuery = true)
    List<LIMSResult> getLIMSResultByManifestRecordIdAndSampleId(Integer  manifestId,  String sampleId);

    @Query(value = "SELECT  CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM  laboratory_result  WHERE   test_id =:testId and archived = 0", nativeQuery = true)
    boolean existsByTestId(@Param("testId") Integer testId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE laboratory_result SET " +
            "result_report = :testResult, " +
            "result_reported = :testResult, " +
            "date_assayed = :assayDate , " +
            "date_result_reported = :reportedDate, " +
            "date_result_received = NOW(), " +
            "created_by = 'lims', " +
            "modified_by = 'lims', " +
            "date_created = NOW(), " +
            "date_modified = NOW(), " +
            "pcr_lab_sample_number = :pcrLabSampleNumber, " +
            "approved_by = :approvedBy, " +
            "archived = 0, " +
            "date_approved = :dateResultDispatched " +
            "WHERE  test_id=:testId",
            nativeQuery = true)
    void updateLabResultNative(
            @Param("testResult") String testResult,
            @Param("reportedDate") LocalDateTime reportedDate,
            @Param("assayDate") LocalDateTime assayDate,
            @Param("dateResultDispatched") LocalDateTime dateResultDispatched,
            @Param("pcrLabSampleNumber") String pcrLabSampleNumber,
            @Param("approvedBy") String approvedBy,
            @Param("testId") Integer  testId
    );

    @Modifying
    @Query(value = "INSERT INTO laboratory_result (" +
            "uuid,facility_id, test_id,patient_id, patient_uuid, result_report, result_reported, date_assayed, " +
            "date_result_reported, date_result_received, created_by, " +
            "modified_by, date_created, date_modified, pcr_lab_sample_number, " +
            "approved_by, archived, date_approved) " +
            "VALUES (:uuid, :facilityId, :testId, :patientId, :patientUuid, :testResult, :testResult, :assayDate, " +
            ":reportedDate, NOW(), 'lims', 'lims', NOW(), NOW(), " +
            ":pcrLabSampleNumber, :approvedBy, 0, :dateResultDispatched)",
            nativeQuery = true)
    void insertLabResultNative(
            @Param("uuid") String uuid,
            @Param("facilityId") long  facilityId,
            @Param("testId") Integer testId,
            @Param("patientId") Integer  patientId,
            @Param("patientUuid") String  patientUuid,
            @Param("testResult") String testResult,
            @Param("reportedDate") LocalDateTime reportedDate,
            @Param("assayDate") LocalDateTime assayDate,
            @Param("dateResultDispatched") LocalDateTime dateResultDispatched,
            @Param("pcrLabSampleNumber") String pcrLabSampleNumber,
            @Param("approvedBy") String approvedBy
    );



    @Transactional
    @Modifying
    @Query(value="insert into laboratory_result(uuid, date_assayed, date_result_reported, date_result_received, result_reported, test_id, patient_uuid, facility_id, patient_id)\n" +
            "values(:uuid, :date_assayed, :date_result_reported, :date_result_received, :result_reported, :test_id, :patient_uuid, :facility_id, :patient_id)", nativeQuery = true)
    void SaveSampleResult(@Param("uuid") String uuid,
                          @Param("date_assayed") LocalDateTime dateAssayed,
                          @Param("date_result_reported") LocalDateTime dateResultReported,
                          @Param("date_result_received") LocalDateTime dateResultReceived,
                          @Param("result_reported") String resultReported,
                          @Param("test_id") int testId,
                          @Param("patient_uuid") String patientUuid,
                          @Param("facility_id") int facilityId,
                          @Param("patient_id") int patientId);

    @Transactional
    @Modifying
    @Query(value="update laboratory_test set lab_test_order_status=5 where id=:test_id ", nativeQuery = true)
    void UpdateTestStatus(@Param("test_id") int testId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE laboratory_result " +
            "SET date_assayed = :date_assayed, " +
            "    date_result_reported = :date_result_reported, " +
            "    date_result_received = :date_result_received, " +
            "    result_reported = :result_reported " +
            "WHERE test_id = :test_id AND patient_uuid = :patient_uuid", nativeQuery = true)
    void updateSampleResultByTestAndPatient(
                                            @Param("date_assayed") LocalDateTime dateAssayed,
                                            @Param("date_result_reported") LocalDateTime dateResultReported,
                                            @Param("date_result_received") LocalDateTime dateResultReceived,
                                            @Param("result_reported") String resultReported,
                                            @Param("test_id") int testId,
                                            @Param("patient_uuid") String patientUuid );
}
