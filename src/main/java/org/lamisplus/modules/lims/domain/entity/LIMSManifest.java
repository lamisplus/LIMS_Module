package org.lamisplus.modules.lims.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "lims_manifest")
public class LIMSManifest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private String uuid;
    @Column(name = "manifest_id")
    private String manifestID;
    @Column(name = "sending_facility_id")
    private String sendingFacilityID;
    @Column(name = "sending_facility_name")
    private String sendingFacilityName;
    @Column(name = "receiving_lab_id")
    private String receivingLabID;
    @Column(name = "receiving_lab_name")
    private String receivingLabName;
    @Column(name = "date_scheduled_for_pickup")
    private String dateScheduledForPickup;
    @Column(name = "temperature_at_pickup")
    private String temperatureAtPickup;
    @Column(name = "sample_packaged_by")
    private String samplePackagedBy;
    @Column(name = "courier_rider_name")
    private String courierRiderName;
    @Column(name = "courier_contact")
    private String courierContact;
    @Column(name = "manifest_status")
    private String manifestStatus;
    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "facility_id")
    private Long facilityId;
    @Column(name = "facility_uuid")
    private String facilityUuid;

    @JoinColumn(name = "manifest_record_id")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LIMSSample> sampleInformation;
}
