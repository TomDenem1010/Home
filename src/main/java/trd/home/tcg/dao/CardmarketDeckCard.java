package trd.home.tcg.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(name = "cardmarket_deck_version_card")
@Getter
@Setter
public class CardmarketDeckCard extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_version_id", nullable = false)
    private CardmarketDeckVersion deckVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CardmarketCard card;

    @Column(nullable = false)
    private int quantity;
}
