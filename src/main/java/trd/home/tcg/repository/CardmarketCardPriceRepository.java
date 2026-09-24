package trd.home.tcg.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketCardPrice;

public interface CardmarketCardPriceRepository extends JpaRepository<CardmarketCardPrice, String> {

    Optional<CardmarketCardPrice> findFirstByCardIdOrderByCreatedAtDescIdDesc(String cardId);
}
