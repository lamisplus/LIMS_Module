package org.lamisplus.modules.lims.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;

public interface LIMSSampleProjection {
    Long getId();
    String getManifestRecordID();
    String getSampleID();
    String getTestID();
    String getUuid();
    String getDateOfBirth();
    String getPid();
    String getPatientID();
    String getSampleType();
    String getSampleOrderedBy();
    String getSampleOrderDate();
    String getSampleCollectedBy();
    String getSampleCollectionDate();
    String getSampleCollectionTime();
    String getDateSampleSent();
    String getIndicationVLTest();
    String getFirstName();
    String getSurName();
    String getSex();
    Integer getAge();
    String getHospitalNumber();
    String getDrugRegimen();
    Long getSendingFacilityID();
    String getSendingFacilityName();
    String getPriority();
    String getPriorityReason();
    String getUniqueId();
}
