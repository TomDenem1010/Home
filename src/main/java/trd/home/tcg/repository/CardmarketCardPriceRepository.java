package trd.home.tcg.repository;

import jakarta.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dto.CardSearchPrice;

public interface CardmarketCardPriceRepository extends JpaRepository<CardmarketCardPrice, String> {

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "500"))
    Stream<CardSearchPrice> findAllByCardIdInOrderByCreatedAtDescIdDesc(Collection<String> cardIds);

    @EntityGraph(attributePaths = "card")
    Optional<CardmarketCardPrice> findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
            String fromCardId, BigDecimal excludedFromPrice, String trendCardId, BigDecimal excludedTrendPrice);
}
