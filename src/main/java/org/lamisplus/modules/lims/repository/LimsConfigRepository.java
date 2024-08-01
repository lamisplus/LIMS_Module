package org.lamisplus.modules.lims.repository;

import org.lamisplus.modules.lims.domain.entity.LIMSConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimsConfigRepository extends JpaRepository<LIMSConfig, Integer> {
}
