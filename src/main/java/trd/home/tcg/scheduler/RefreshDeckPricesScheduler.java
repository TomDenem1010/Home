package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.service.event.RefreshDeckPricesService;

@Component
@RequiredArgsConstructor
public class RefreshDeckPricesScheduler {

    private final ApplicationEventRepository eventRepository;
    private final RefreshDeckPricesService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.refresh-prices.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO)
                .ifPresent(service::process);
    }
}
