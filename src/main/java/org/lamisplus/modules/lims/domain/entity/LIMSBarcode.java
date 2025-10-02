package org.lamisplus.modules.lims.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@Data
@Table(name = "lims_barcode")
@NoArgsConstructor
public class LIMSBarcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private String uuid;
    @Column(name = "manifest_id")
    private String manifestId;
    @Column(name = "sample_id")
    private String sampleId;
    @Column(name = "test_id")
    private Integer testId;
    @Column(name = "serial_number")
    private Integer serialNumber;
}
