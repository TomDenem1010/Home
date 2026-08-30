package trd.home.tcg.dao;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.exception.WrongCardQuantityException;

@Entity
@Table(name = "cardmarket_deck_version")
@Getter
@Setter
public class CardmarketDeckVersion extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private CardmarketDeck deck;

    private String version;

    @OneToMany(mappedBy = "deckVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardmarketDeckCard> cards = new LinkedHashSet<>();

    @LogMethodCall
    public void addCard(CardmarketCard card, int quantity) {
        if (quantity <= 0) {
            throw new WrongCardQuantityException("Card quantity must be positive.");
        }

        CardmarketDeckCard deckCard = new CardmarketDeckCard();
        deckCard.setDeckVersion(this);
        deckCard.setCard(card);
        deckCard.setQuantity(quantity);
        cards.add(deckCard);
    }
}
