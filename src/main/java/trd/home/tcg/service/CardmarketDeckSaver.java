package trd.home.tcg.service;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class CardmarketDeckSaver {

    private final CardmarketDeckRepository deckRepository;
    private final CardmarketCardRepository cardRepository;

    @Transactional
    @LogMethodCall
    public void save(CardmarketDeck deck) {
        if (deckRepository.existsByName(deck.getName())) {
            deckRepository.findByName(deck.getName()).ifPresent(existingDeck -> {
                CardmarketDeckVersion importedVersion = deck.getCurrentVersion();
                if (isNewer(
                        importedVersion.getVersion(),
                        existingDeck.getCurrentVersion().getVersion())) {
                    saveCards(importedVersion);
                    existingDeck.addVersion(importedVersion);
                    deckRepository.save(existingDeck);
                }
            });
            return;
        }

        deck.getVersions().forEach(this::saveCards);
        deckRepository.save(deck);
    }

    private void saveCards(CardmarketDeckVersion version) {
        version.getCards().forEach(deckCard -> {
            CardmarketCard card = deckCard.getCard();
            CardmarketCard persistedCard = cardRepository
                    .findByLinkAndFoilType(card.getLink(), card.getFoilType())
                    .orElseGet(() -> cardRepository.save(card));
            deckCard.setCard(persistedCard);
        });
    }

    private boolean isNewer(String importedVersion, String existingVersion) {
        return versionNumber(importedVersion).compareTo(versionNumber(existingVersion)) > 0;
    }

    private BigInteger versionNumber(String version) {
        return new BigInteger(version.replaceFirst("^[vV]", ""));
    }
}
