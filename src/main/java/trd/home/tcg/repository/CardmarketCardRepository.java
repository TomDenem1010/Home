package trd.home.tcg.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dto.CardmarketCardDto;

public interface CardmarketCardRepository extends JpaRepository<CardmarketCard, String> {

    Optional<CardmarketCard> findByLinkAndFoilType(String link, CardFoilType foilType);

    default Optional<CardmarketCardDto> findByUuid(String uuid) {
        return findById(uuid).map(CardmarketCardDto::from);
    }
}
