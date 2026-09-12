package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
}
