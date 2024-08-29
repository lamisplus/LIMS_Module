package org.lamisplus.modules.lims.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "lims_config")
public class LIMSConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private String uuid;
    @Column(name = "config_name")
    private String configName;
    @Column(name = "config_email")
    private String configEmail;
    @Column(name = "config_password")
    private String configPassword;
    @Column(name = "server_url")
    private String serverUrl;
    @Column(name = "test_facility_name")
    private String testFacilityName;
    @Column(name = "test_facility_datim_code")
    private String testFacilityDATIMCode;
    @Column(name = "create_date")
    private LocalDateTime createDate;
}
