package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findBySlug(String slug);
    List<Platform> findByActiveTrueOrderByNameAsc();
}
