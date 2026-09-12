package com.marketinganalytics.platform.repository.spec;

import com.marketinganalytics.platform.dto.lead.LeadFilter;
import com.marketinganalytics.platform.entity.Lead;
import org.springframework.data.jpa.domain.Specification;

public final class LeadSpecifications {

    private LeadSpecifications() {
    }

    public static Specification<Lead> fromFilter(LeadFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().trim().toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("company")), like)
                ));
            }
            if (filter.status() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.status()));
            }
            if (filter.source() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("source"), filter.source()));
            }
            if (filter.campaignId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("campaign").get("id"), filter.campaignId()));
            }
            return predicates;
        };
    }
}
