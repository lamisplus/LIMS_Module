package org.lamisplus.modules.lims.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "lims_result")
public class LIMSResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private String uuid;
    @Column(name = "sample_id")
    private String sampleID;
    @Column(name = "pcr_lab_sample_number")
    private String pcrLabSampleNumber;
    @Column(name = "visit_date")
    private String visitDate;
    @Column(name = "date_sample_received_at_pcr_lab")
    private String dateSampleReceivedAtPcrLab;
    @Column(name = "result_date")
    private String resultDate;
    @Column(name = "test_result")
    private String testResult;
    @Column(name = "assay_date")
    private String assayDate;
    @Column(name = "approval_date")
    private String approvalDate;
    @Column(name = "date_result_dispatched")
    private String dateResultDispatched;
    @Column(name = "sample_status")
    private String sampleStatus;
    @Column(name = "sample_testable")
    private String sampleTestable;
    @Column(name = "manifest_record_id")
    private Integer manifestRecordID;

    @Column(name = "transfer_status")
    private String transferStatus;
    @Column(name = "tested_by")
    private String  testedBy;
    @Column(name = "approved_by")
    private String approvedBy;
    //@Column(name = "date_transferred_out")
    //private String dateTransferredOut;
    @Column(name = "reason_not_tested")
    private String reasonNotTested;
    @Column(name = "other_rejection_reason")
    private String otherRejectionReason;
    @Column(name = "sending_pcr_lab_id")
    private String sendingPcrLabID;
    @Column(name = "sending_pcr_lab_name")
    private String sendingPcrLabName;

    @Column(name = "secondary_pcr_lab_id")
    private String Secondary_PCR_Lab_ID;
    @Column(name = "secondary_pcr_lab_name")
    private String Secondary_PCR_Lab_Name;
    @Column(name = "date_transferred_out")
    private String Date_Transferred_Out;
    @Column(name = "rejection_reason")
    private String rejectionReason;
}
