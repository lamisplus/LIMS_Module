package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LABSampleDTO {
    private Integer id;
    private String uuid;
    private Integer pid;
    private JsonNode patientID;
    private String firstName;
    private String surName;
    private String Sex;
    private String pregnantBreastFeedingStatus;
    private String age;
    private String dateOfBirth;
    private String sampleID;
    private String sampleType;
    private String indicationVLTest;
    private String artCommencementDate;
    private String drugRegimen;
    private String sampleOrderedBy;
    private String sampleOrderDate;
    private String sampleCollectedBy;
    private String sampleCollectionDate;
    private String sampleCollectionTime;
    private String dateSampleSent;
    private String priority;
    private String priorityReason;
    private Integer manifestRecordId;
    private Integer testID;
    private String  facilityId;
    private String  hospitalNumber;
    private String uniqueId;
}


