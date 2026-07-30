package trd.home.tcg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dto.CardmarketCardDto;

public interface CardmarketCardRepository extends JpaRepository<CardmarketCard, String> {

    @Query("""
            SELECT new trd.home.tcg.dto.CardmarketCardDto(card.id, card.link)
            FROM CardmarketCard card
            WHERE card.id = :uuid
            """)
    Optional<CardmarketCardDto> findByUuid(@Param("uuid") String uuid);
}
