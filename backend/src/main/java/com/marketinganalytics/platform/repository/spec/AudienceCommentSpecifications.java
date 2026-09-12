package com.marketinganalytics.platform.repository.spec;

import com.marketinganalytics.platform.dto.comment.CommentFilter;
import com.marketinganalytics.platform.entity.AudienceComment;
import org.springframework.data.jpa.domain.Specification;

public final class AudienceCommentSpecifications {

    private AudienceCommentSpecifications() {
    }

    public static Specification<AudienceComment> fromFilter(CommentFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.platformId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("platform").get("id"), filter.platformId()));
            }
            if (filter.sentiment() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("sentiment"), filter.sentiment()));
            }
            if (filter.search() != null && !filter.search().isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("text")), "%" + filter.search().trim().toLowerCase() + "%"));
            }
            return predicates;
        };
    }
}
