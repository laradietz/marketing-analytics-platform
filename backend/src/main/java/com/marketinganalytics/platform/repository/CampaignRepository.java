package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
    List<Campaign> findByStatusIn(List<CampaignStatus> statuses);
    List<Campaign> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);
    long countByStatus(CampaignStatus status);
    List<Campaign> findByPlatformIdAndExternalCampaignIdIsNotNull(Long platformId);
}
