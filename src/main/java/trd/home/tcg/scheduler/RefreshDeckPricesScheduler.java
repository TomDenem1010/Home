package trd.home.tcg.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Slf4j
@Component
public class RefreshDeckPricesScheduler {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketCardPriceSaver cardPriceSaver;
    private final CardmarketCardRepository cardRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public RefreshDeckPricesScheduler(
            ApplicationEventRepository eventRepository,
            CardmarketCardPriceSaver cardPriceSaver,
            CardmarketCardRepository cardRepository,
            FrontendNotificationPublisher notificationPublisher) {
        this.eventRepository = eventRepository;
        this.cardPriceSaver = cardPriceSaver;
        this.cardRepository = cardRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Scheduled(fixedDelayString = "${tcg.scheduler.refresh-prices.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    FrontendNotificationType notificationType;
                    String notificationMessage;
                    try {
                        cardPriceSaver.updateCardPrice(cardRepository.findAllInActiveDeckCurrentVersions());
                        event.markDone();
                        notificationType = FrontendNotificationType.SUCCESS;
                        notificationMessage = "Deck prices were refreshed successfully.";
                    } catch (RuntimeException exception) {
                        log.error("Failed to refresh Cardmarket prices for active decks", exception);
                        event.markFailed(exception);
                        notificationType = FrontendNotificationType.ERROR;
                        notificationMessage = "Failed to refresh deck prices: " + exception.getMessage();
                    }
                    eventRepository.save(event);
                    notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
                });
    }
}
