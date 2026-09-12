package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.CampaignMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignMetricRepository extends JpaRepository<CampaignMetric, Long> {

    List<CampaignMetric> findByCampaignIdOrderByRecordedDateAsc(Long campaignId);

    Optional<CampaignMetric> findByCampaignIdAndRecordedDate(Long campaignId, LocalDate recordedDate);

    @Query("select m from CampaignMetric m where m.recordedDate between :from and :to")
    List<CampaignMetric> findAllBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select m from CampaignMetric m where m.campaign.id in :campaignIds and m.recordedDate between :from and :to")
    List<CampaignMetric> findByCampaignIdsBetween(@Param("campaignIds") List<Long> campaignIds,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    @Query("select m from CampaignMetric m where m.campaign.platform.id in :platformIds and m.recordedDate between :from and :to")
    List<CampaignMetric> findByPlatformIdsBetween(@Param("platformIds") List<Long> platformIds,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    @Query("select m from CampaignMetric m where m.campaign.platform.id = :platformId and m.recordedDate = :date")
    List<CampaignMetric> findByPlatformIdAndRecordedDate(@Param("platformId") Long platformId,
                                                          @Param("date") LocalDate date);
}
