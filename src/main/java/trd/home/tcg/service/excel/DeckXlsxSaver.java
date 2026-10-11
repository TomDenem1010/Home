package trd.home.tcg.service.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class DeckXlsxSaver {
    private final CardmarketDeckRepository decks;
    private final CardmarketCardRepository cards;
    private final CardmarketCardPriceRepository prices;

    @Transactional
    public boolean save(DeckExcelData source) {
        var existing = decks.findByName(source.name());
        if (existing.isPresent()
                && existing.get().getCurrentVersion() != null
                && DeckXlsxReader.versionNumber(source.version())
                                .compareTo(DeckXlsxReader.versionNumber(
                                        existing.get().getCurrentVersion().getVersion()))
                        <= 0) {
            return false;
        }
        var deck = existing.orElseGet(() -> {
            var created = new CardmarketDeck();
            created.setName(source.name());
            return created;
        });
        var version = new CardmarketDeckVersion();
        version.setVersion(source.version());
        for (var sourceCard : source.cards()) {
            var card = cards.findByLinkAndFoilType(sourceCard.link(), sourceCard.foilType())
                    .orElseGet(() -> {
                        var created = new CardmarketCard();
                        created.setLink(sourceCard.link());
                        created.setFoilType(sourceCard.foilType());
                        return cards.save(created);
                    });
            version.addCard(card, sourceCard.quantity());
            savePrice(card, sourceCard);
        }
        deck.addVersion(version);
        decks.save(deck);
        return true;
    }

    private void savePrice(CardmarketCard card, DeckExcelData.Card source) {
        if (source.fromEuro() == null && source.trendEuro() == null) {
            return;
        }
        var price = prices.findByCardId(card.getId()).orElseGet(() -> {
            var created = new CardmarketCardPrice();
            created.setCard(card);
            return created;
        });
        price.setFromInEuro(source.fromEuro());
        price.setTrendInEuro(source.trendEuro());
        prices.save(price);
    }
}
