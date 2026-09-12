package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.AudienceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AudienceCommentRepository extends JpaRepository<AudienceComment, Long>, JpaSpecificationExecutor<AudienceComment> {
    List<AudienceComment> findByPlatformIdOrderByCreatedAtDesc(Long platformId);
}
