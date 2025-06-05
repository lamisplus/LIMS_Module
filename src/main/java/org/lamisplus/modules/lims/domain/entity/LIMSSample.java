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
@SqlResultSetMapping(
        name = "LIMSSampleMapping",
        classes = @ConstructorResult(
                targetClass = LIMSSample.class,
                columns = {
                        @ColumnResult(name = "id", type = Integer.class),
                        @ColumnResult(name = "manifest_record_id", type = String.class),
                        @ColumnResult(name = "sample_id", type = String.class),
                        @ColumnResult(name = "test_id", type = Integer.class),
                        @ColumnResult(name = "uuid", type = String.class),
                        @ColumnResult(name = "date_of_birth", type = String.class),
                        @ColumnResult(name = "pid", type = Integer.class),
                        @ColumnResult(name = "patient_id", type = JsonNodeBinaryType.class),
                        @ColumnResult(name = "sample_type", type = String.class),
                        @ColumnResult(name = "sample_ordered_by", type = String.class),
                        @ColumnResult(name = "sample_order_date", type = String.class),
                        @ColumnResult(name = "sample_collected_by", type = String.class),
                        @ColumnResult(name = "sample_collection_date", type = String.class),
                        @ColumnResult(name = "sample_collection_time", type = String.class),
                        @ColumnResult(name = "date_sample_sent", type = String.class),
                        @ColumnResult(name = "indication_vl_test", type = String.class),
                        @ColumnResult(name = "first_name", type = String.class),
                        @ColumnResult(name = "surname", type = String.class),
                        @ColumnResult(name = "sex", type = String.class),
                        @ColumnResult(name = "age", type = String.class),
                        @ColumnResult(name = "hospital_number", type = String.class),
                        @ColumnResult(name = "drug_regimen", type = String.class),
                        @ColumnResult(name = "sending_facility_id", type = Long.class),
                        @ColumnResult(name = "sending_facility_name", type = String.class),
                        @ColumnResult(name = "priority", type = String.class),
                        @ColumnResult(name = "priority_reason", type = String.class),
                        @ColumnResult(name = "unique_id", type = String.class)
                }
        )
)
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
    @Column(name = "test_id")
    private Integer testID;
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
    private String age;
    @Column(name = "date_of_birth")
    private String dateOfBirth;
    @Column(name = "pregnant_breast_feeding_status")
    private String pregnantBreastFeedingStatus;
    @Column(name = "art_commencement_date")
    private String artCommencementDate;
    @Column(name = "drug_regimen")
    private String drugRegimen;
    @Column(name = "sending_facility_id")
    private String facilityId;
    @Column(name = "sending_facility_name")
    private String sendingFacilityName;
    @Column(name = "priority")
    private String priority;
    @Column(name = "priority_reason")
    private String priorityReason;
    @Column(name = "manifest_record_id")
    private Integer manifestRecordID;
    @Column(name = "hospital_number")
    String hospitalNumber;
    @Column(name = "unique_id")
    String uniqueId;
    public LIMSSample(
            Integer id,
            String manifestRecordID,
            String sampleID,
            Integer testID,
            String uuid,
            String dateOfBirth,
            Integer pid,
            JsonNode patientID,
            String sampleType,
            String sampleOrderedBy,
            String sampleOrderDate,
            String sampleCollectedBy,
            String sampleCollectionDate,
            String sampleCollectionTime,
            String dateSampleSent,
            String indicationVLTest,
            String firstName,
            String surName,
            String sex,
            String age,
            String hospitalNumber,
            String drugRegimen,
            Long sendingFacilityID,
            String sendingFacilityName,
            String priority,
            String priorityReason,
            String uniqueId
    ) {
        this.id = id;
        this.manifestRecordID = manifestRecordID != null ? Integer.valueOf(manifestRecordID) : null;
        this.sampleID = sampleID;
        this.testID = testID;
        this.uuid = uuid;
        this.dateOfBirth = dateOfBirth;
        this.pid = String.valueOf(pid);
        this.patientID = patientID;
        this.sampleType = sampleType;
        this.sampleOrderedBy = sampleOrderedBy;
        this.sampleOrderDate = sampleOrderDate;
        this.sampleCollectedBy = sampleCollectedBy;
        this.sampleCollectionDate = sampleCollectionDate;
        this.sampleCollectionTime = sampleCollectionTime;
        this.dateSampleSent = dateSampleSent;
        this.indicationVLTest = indicationVLTest;
        this.firstName = firstName;
        this.surName = surName;
        this.Sex = sex;
        this.age = age;
        this.hospitalNumber = hospitalNumber;
        this.drugRegimen = drugRegimen;
        this.facilityId = sendingFacilityID != null ? String.valueOf(sendingFacilityID) : null;
        this.sendingFacilityName = sendingFacilityName;
        this.priority = priority;
        this.priorityReason = priorityReason;
        this.uniqueId = uniqueId;
    }
}