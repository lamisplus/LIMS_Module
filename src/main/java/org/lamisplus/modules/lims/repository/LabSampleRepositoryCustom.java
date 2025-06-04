package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.dto.LABSampleDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface LabSampleRepositoryCustom {
    Page<LABSampleDTO> findLabSamples(Long facilityId, LocalDate startDate, LocalDate endDate, int pageSize, int  offset);
}
