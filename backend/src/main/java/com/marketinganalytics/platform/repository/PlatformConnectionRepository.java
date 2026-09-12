package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.PlatformConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformConnectionRepository extends JpaRepository<PlatformConnection, Long> {
    Optional<PlatformConnection> findByPlatformId(Long platformId);
}
