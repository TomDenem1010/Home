package trd.home.tcg.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dto.CardSearchFilter;

public final class CardSearchSpecification {
    private CardSearchSpecification() {}

    public static Specification<CardmarketDeckCard> matching(List<String> versionIds, CardSearchFilter filter) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(root.get("deckVersion").get("id").in(versionIds));
            var card = root.join("card");
            if (filter.foilType() != null) {
                predicates.add(builder.equal(card.get("foilType"), filter.foilType()));
            }
            Expression<String> link = card.get("link");
            // Percent-encoded paths require URI decoding in the exact Java check.
            Predicate encodedLink = builder.like(link, "%!%%", '!');
            if (filter.cardGameType() != null) {
                predicates.add(builder.or(
                        encodedLink,
                        builder.like(
                                link, "%/" + escapeLike(filter.cardGameType().getCardmarketUrlPart()) + "/%", '!')));
            }
            if (filter.name() != null && !filter.name().isBlank()) {
                String normalized = filter.name()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[-\\s]+", " ")
                        .strip();
                for (String word : normalized.split(" ")) {
                    predicates.add(builder.or(
                            encodedLink, builder.like(builder.lower(link), "%" + escapeLike(word) + "%", '!')));
                }
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
