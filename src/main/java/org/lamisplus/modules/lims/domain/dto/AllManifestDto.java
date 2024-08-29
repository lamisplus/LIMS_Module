package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class AllManifestDto implements Serializable
{
    private Integer localManifestId;
    private String manifestID;
    private String sendingFacilityID;
    private String sendingFacilityName;
    private String receivingLabID;
    private String receivingLabName;
    private String dateScheduledForPickup;
    private String temperatureAtPickup;
    private String samplePackagedBy;
    private String courierRiderName;
    private String courierContact;
    private String manifestStatus;
    private LocalDateTime createDate;
    private Long facilityId;

    private JsonNode patientID;
    private Integer localSampleId;
    private String pid;
    private String sampleType;
    private String sampleOrderedBy;
    private String sampleOrderDate;
    private String sampleCollectedBy;
    private String sampleCollectionDate;
    private String sampleCollectionTime;
    private String dateSampleSent;
    private String indicationVLTest;
    private String firstName;
    private String surName;
    private String Sex;
    private String Age;
    private String dateOfBirth;
    private String pregnantBreastFeedingStatus;
    private String artCommencementDate;
    private String priority;
    private String priorityReason;
    private boolean resultIsBack;

    private Integer localResultId;
    private String sampleID;
    private String pcrLabSampleNumber;
    private String visitDate;
    private String dateSampleReceivedAtPcrLab;
    private String resultDate;
    private String testResult;
    private String assayDate;
    private String approvalDate;
    private String dateResultDispatched;
    private String sampleStatus;
    private String sampleTestable;
    private String transferStatus;
    private String  testedBy;
    private String approvedBy;
    private String dateTransferredOut;
    private String reasonNotTested;
    private String otherRejectionReason;
    private String sendingPcrLabID;
    private String sendingPcrLabName;




}
