package org.lamisplus.modules.lims.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "lims_sample")
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class),
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class),
        @TypeDef(name = "json-node", typeClass = JsonNodeStringType.class),
})
public class LIMSSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private String uuid;
    @Column(name = "pid")
    private String pid;
    @Column(name = "sample_id")
    private String sampleID;

    @Type(type = "jsonb-node")
    @Column(columnDefinition = "jsonb", name = "patient_id")
    private JsonNode patientID;

    @Column(name = "sample_type")
    private String sampleType;
    @Column(name = "sample_ordered_by")
    private String sampleOrderedBy;
    @Column(name = "sample_order_date")
    private String sampleOrderDate;
    @Column(name = "sample_collected_by")
    private String sampleCollectedBy;
    @Column(name = "sample_collection_date")
    private String sampleCollectionDate;
    @Column(name = "sample_collection_time")
    private String sampleCollectionTime;
    @Column(name = "date_sample_sent")
    private String dateSampleSent;
    @Column(name = "indication_vl_test")
    private String indicationVLTest;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "surname")
    private String surName;
    @Column(name = "sex")
    private String Sex;
    @Column(name = "age")
    private String Age;
    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "pregnant_breast_feeding_status")
    private String pregnantBreastFeedingStatus;
    @Column(name = "art_commencement_date")
    private String artCommencementDate;
    @Column(name = "drug_regimen")
    private String drugRegimen;

    @Column(name = "sending_facility_id")
    private String sendingFacilityID;
    @Column(name = "sending_facility_name")
    private String sendingFacilityName;
    @Column(name = "priority")
    private String priority;
    @Column(name = "priority_reason")
    private String priorityReason;

    @Column(name = "manifest_record_id")
    private Integer manifestRecordID;
}
