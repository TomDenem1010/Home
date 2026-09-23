package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.RefreshDeckPricesService;

@Component
@RequiredArgsConstructor
public class RefreshDeckPricesScheduler {

    private final ApplicationEventQueue eventQueue;
    private final RefreshDeckPricesService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.refresh-prices.delay:5s}")
    public void processNextEvent() {
        eventQueue.claimNext(EventType.REFRESH_DECK_PRICES).ifPresent(service::process);
    }
}
