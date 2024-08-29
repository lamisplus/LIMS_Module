package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class LABSampleDTO {
    private Integer id;
    private String uuid;
    private Integer pid;
    private JsonNode patientID;
    private String firstName;
    private String surName;
    private String Sex;
    private String pregnantBreastFeedingStatus;
    private String Age;
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
}
