package trd.home.tcg.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketCardPrice;

public interface CardmarketCardPriceRepository extends JpaRepository<CardmarketCardPrice, String> {

    @EntityGraph(attributePaths = "card")
    Optional<CardmarketCardPrice> findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
            String fromCardId, BigDecimal excludedFromPrice, String trendCardId, BigDecimal excludedTrendPrice);
}
