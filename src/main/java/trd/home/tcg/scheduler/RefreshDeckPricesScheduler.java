package trd.home.tcg.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Component
public class RefreshDeckPricesScheduler {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketCardPriceSaver cardPriceSaver;
    private final CardmarketCardRepository cardRepository;

    public RefreshDeckPricesScheduler(
            ApplicationEventRepository eventRepository,
            CardmarketCardPriceSaver cardPriceSaver,
            CardmarketCardRepository cardRepository) {
        this.eventRepository = eventRepository;
        this.cardPriceSaver = cardPriceSaver;
        this.cardRepository = cardRepository;
    }

    @Scheduled(fixedDelayString = "${tcg.scheduler.refresh-prices.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    try {
                        cardPriceSaver.updateCardPrice(cardRepository.findAllInActiveDeckCurrentVersions());
                        event.markDone();
                    } catch (RuntimeException exception) {
                        event.markFailed(exception);
                    }
                    eventRepository.save(event);
                });
    }
}
