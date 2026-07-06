package org.lamisplus.modules.lims.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.lims.domain.dto.LABSampleDTO;
import org.lamisplus.modules.lims.domain.dto.PatientIdDTO;
import org.lamisplus.modules.lims.domain.entity.LIMSSample;
import org.lamisplus.modules.lims.domain.mapper.LimsMapper;
import org.lamisplus.modules.lims.repository.LabSampleRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LabSampleRepositoryImpl implements LabSampleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    private final LimsMapper limsMapper;

    @Override
    public Page<LABSampleDTO> findLabSamples(Long facilityId, LocalDate startDate, LocalDate endDate, int pageSize, int pageNo) {
        int offset = (pageNo * pageSize);
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) ");
        countSql.append("FROM laboratory_order a ");
        countSql.append("INNER JOIN laboratory_test b ON a.id = b.lab_order_id ");
        countSql.append("INNER JOIN laboratory_sample c ON b.id = c.test_id ");
        countSql.append("INNER JOIN laboratory_labtest d ON b.lab_test_id = d.id ");
        countSql.append("INNER JOIN laboratory_sample_type e ON c.sample_type_id = e.id ");
        countSql.append("INNER JOIN patient_person p ON p.id = a.patient_id ");
        countSql.append("INNER JOIN hiv_enrollment_commencement en ON en.person_uuid = p.uuid ");
        countSql.append("LEFT JOIN base_application_codeset f ON b.viral_load_indication = f.id ");
        countSql.append("WHERE d.lab_test_name = 'Viral Load' AND a.facility_id = ?1 AND b.lab_test_order_status IN (1, 2, 3) AND c.sample_number NOT IN (SELECT sample_id FROM lims_sample) ");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append("  c.id AS id, ")
                .append("  c.uuid AS uuid, ")
                .append("  '' AS priority, ")
                .append("  p.id AS pid, ")
                .append("  NULL AS patient_id, ")
                .append("  p.first_name AS first_name, ")
                .append("  p.surname AS surname, ")
                .append("  p.sex AS sex, ")
                .append("  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS TEXT) AS age, ")
                .append("  CAST(p.date_of_birth AS TEXT) AS date_of_birth, ")
                .append("  c.sample_number AS sample_id, ")
                .append("  e.sample_type_name AS sample_type, ")
                .append("  f.display AS indication_vl_test, ")
                .append("  '' AS art_commencement_date, ")
                .append("  '' AS drug_regimen, ")
                .append("  a.userid AS sample_ordered_by, ")
                .append("  CAST(a.order_date AS TEXT) AS sample_order_date, ")
                .append("  c.sample_collected_by AS sample_collected_by, ")
                .append("  CAST(c.date_sample_collected AS TEXT) AS sample_collection_date, ")
                .append("  '00:00:00' AS sample_collection_time, ")
                .append("  '' AS date_sample_sent, ")
                .append("  en.unique_id AS unique_id, ")
                .append("  '' AS priority_reason, ")
                .append("  '' AS sending_facility_name, ")
                .append("  '' AS priority, ")
                .append("  '0' AS manifest_record_id, ")
                .append("  b.id AS test_id, ")
                .append("  CAST(p.facility_id AS TEXT) AS sending_facility_id, ")
                .append("  p.hospital_number AS hospital_number ")
                .append("FROM laboratory_order a ")
                .append("INNER JOIN laboratory_test b ON a.id = b.lab_order_id ")
                .append("INNER JOIN laboratory_sample c ON b.id = c.test_id ")
                .append("INNER JOIN laboratory_labtest d ON b.lab_test_id = d.id ")
                .append("INNER JOIN laboratory_sample_type e ON c.sample_type_id = e.id ")
                .append("INNER JOIN patient_person p ON p.id = a.patient_id ")
                .append("INNER JOIN hiv_enrollment_commencement en ON en.person_uuid = p.uuid ")
                .append("LEFT JOIN base_application_codeset f ON b.viral_load_indication = f.id ")
                .append("WHERE d.lab_test_name = 'Viral Load' ")
                .append("  AND a.facility_id = ?1 ")
                .append("  AND b.lab_test_order_status IN (1, 2, 3) ")
                .append("  AND c.sample_number NOT IN (SELECT sample_id FROM lims_sample) ");
        int paramIndex = 2;

        if (startDate != null) {
            sql.append(" AND c.date_sample_collected >= ?").append(paramIndex).append(" ");
            countSql.append(" AND c.date_sample_collected >= ?").append(paramIndex).append(" ");
            paramIndex++;
        }
        if (endDate != null) {
            sql.append(" AND c.date_sample_collected <= ?").append(paramIndex).append(" ");
            countSql.append(" AND c.date_sample_collected <= ?").append(paramIndex).append(" ");
            paramIndex++;
        }
        sql.append("ORDER BY c.date_sample_collected DESC ");
        sql.append("LIMIT ?").append(paramIndex).append(" OFFSET ?").append(paramIndex + 1);

        Query query = entityManager.createNativeQuery(sql.toString(), "LIMSSampleMapping");

        Query countQuery = entityManager.createNativeQuery(countSql.toString());

        int i = 1;
        int j = 1;
        query.setParameter(i++, facilityId);
        countQuery.setParameter(j++, facilityId);

        if (startDate != null) {
            query.setParameter(i++, startDate);
            countQuery.setParameter(j++, startDate);
        }
        if (endDate != null) {
            query.setParameter(i++, endDate);
            countQuery.setParameter(j++, endDate);
        }

        query.setParameter(i++, pageSize);
        query.setParameter(i++, offset);
        Number totalCount = ((Number) countQuery.getSingleResult());
        List<LIMSSample> samples = query.getResultList();
        samples.forEach(this::appendPatientIdDetails);

        List<LABSampleDTO> sampleDTOS = samples.stream()
                .map(limsMapper::tosSampleDto)
                .collect(Collectors.toList());


        return new PageImpl<>(sampleDTOS, PageRequest.of(pageNo, pageSize, Sort.by("sampleID").ascending()), totalCount.longValue());
    }


    private void appendPatientIdDetails(LIMSSample sample) {
        List<PatientIdDTO> patientIdDTOS = new ArrayList<>();

        // Add Hospital Number
        PatientIdDTO hospitalIdDTO = new PatientIdDTO();
        hospitalIdDTO.setIdNumber(sample.getHospitalNumber());
        hospitalIdDTO.setIdTypeCode("HOSPITALNO");
        patientIdDTOS.add(hospitalIdDTO);


        PatientIdDTO clientIdDTO = new PatientIdDTO();
        clientIdDTO.setIdNumber(String.valueOf(sample.getTestID()));
        clientIdDTO.setIdTypeCode("CLIENTID");
        patientIdDTOS.add(clientIdDTO);


        PatientIdDTO recencyDTO = new PatientIdDTO();
        recencyDTO.setIdNumber(sample.getUniqueId());
        recencyDTO.setIdTypeCode("RECENCY");
        patientIdDTOS.add(recencyDTO);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode patientIDNode = mapper.convertValue(patientIdDTOS, JsonNode.class);
        sample.setPatientID(patientIDNode);
    }
}
