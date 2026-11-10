package trd.home.boardgame.specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import trd.home.boardgame.dao.BoardGamePlay;
import trd.home.boardgame.dto.BoardGamePlaySearchFilter;

public final class BoardGamePlaySearchSpecification {
    private BoardGamePlaySearchSpecification() {}

    public static Specification<BoardGamePlay> matching(BoardGamePlaySearchFilter filter) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.name() != null && !filter.name().isBlank()) {
                String name = filter.name()
                        .strip()
                        .toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                predicates.add(builder.like(builder.lower(root.get("boardGame").get("name")), "%" + name + "%", '\\'));
            }
            if (filter.from() != null) predicates.add(builder.greaterThanOrEqualTo(root.get("time"), filter.from()));
            if (filter.to() != null) predicates.add(builder.lessThanOrEqualTo(root.get("time"), filter.to()));
            if (filter.ratingMin() != null) predicates.add(builder.ge(root.get("rating"), filter.ratingMin()));
            if (filter.ratingMax() != null) predicates.add(builder.le(root.get("rating"), filter.ratingMax()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
