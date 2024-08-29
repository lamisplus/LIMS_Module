package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import javax.persistence.Column;

@Data
public class LIMSResultDTO {
    private JsonNode patientID;
    private String firstName;
    private String surName;
    private String sex;
    private String dateOfBirth;
    private String sampleID;
    private String pcrLabSampleNumber;
    private String visitDate;
    private String dateSampleReceivedAtPCRLab;
    private String resultDate;
    private String testResult;
    private String assayDate;
    private String approvalDate;
    private String dateResultDispatched;
    private String sampleStatus;
    private String sampleTestable;
    private Integer manifestRecordID;

    private String transferStatus;
    private String testedBy;
    private String approvedBy;
    //private String dateTransferredOut;
    private String reasonNotTested;
    private String otherRejectionReason;
    private String sendingPcrLabID;
    private String sendingPcrLabName;

    private String Secondary_PCR_Lab_ID;
    private String Secondary_PCR_Lab_Name;
    private String Date_Transferred_Out;
    private String rejectionReason;
}

