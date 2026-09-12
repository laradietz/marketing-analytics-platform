package com.marketinganalytics.platform.repository.spec;

import com.marketinganalytics.platform.dto.content.ContentFilter;
import com.marketinganalytics.platform.entity.Content;
import org.springframework.data.jpa.domain.Specification;

public final class ContentSpecifications {

    private ContentSpecifications() {
    }

    public static Specification<Content> fromFilter(ContentFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.status() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.status()));
            }
            if (filter.platformId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("platform").get("id"), filter.platformId()));
            }
            if (filter.campaignId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("campaign").get("id"), filter.campaignId()));
            }
            return predicates;
        };
    }
}
