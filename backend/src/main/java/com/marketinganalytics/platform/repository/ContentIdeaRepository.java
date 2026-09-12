package com.marketinganalytics.platform.repository;

import com.marketinganalytics.platform.entity.ContentIdea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentIdeaRepository extends JpaRepository<ContentIdea, Long> {
    Page<ContentIdea> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
