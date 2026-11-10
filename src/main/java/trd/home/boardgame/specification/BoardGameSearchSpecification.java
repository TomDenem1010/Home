package trd.home.boardgame.specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import trd.home.boardgame.dao.BoardGame;
import trd.home.boardgame.dto.BoardGameSearchFilter;

public final class BoardGameSearchSpecification {
    private BoardGameSearchSpecification() {}

    public static Specification<BoardGame> matching(BoardGameSearchFilter filter) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.name() != null && !filter.name().isBlank()) {
                String name = filter.name()
                        .strip()
                        .toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + name + "%", '\\'));
            }
            if (filter.state() != null) predicates.add(builder.equal(root.get("state"), filter.state()));
            if (filter.type() != null) predicates.add(builder.equal(root.get("type"), filter.type()));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
