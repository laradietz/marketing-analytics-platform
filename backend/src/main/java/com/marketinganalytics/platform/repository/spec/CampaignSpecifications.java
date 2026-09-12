package com.marketinganalytics.platform.repository.spec;

import com.marketinganalytics.platform.dto.campaign.CampaignFilter;
import com.marketinganalytics.platform.entity.Campaign;
import org.springframework.data.jpa.domain.Specification;

public final class CampaignSpecifications {

    private CampaignSpecifications() {
    }

    public static Specification<Campaign> fromFilter(CampaignFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().trim().toLowerCase() + "%";
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("name")), like));
            }
            if (filter.status() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.status()));
            }
            if (filter.platformId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("platform").get("id"), filter.platformId()));
            }
            if (filter.objective() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("objective"), filter.objective()));
            }
            if (filter.ownerId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("owner").get("id"), filter.ownerId()));
            }
            return predicates;
        };
    }
}
