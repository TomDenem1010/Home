package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ClearDecksService;

@Component
@RequiredArgsConstructor
public class ClearDecksScheduler {
    private final ApplicationEventQueue eventQueue;
    private final ClearDecksService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.clear-decks.delay:5s}")
    public void processNextEvent() {
        eventQueue.claimNext(EventType.CLEAR_DECKS).ifPresent(event -> service.process(event));
    }
}
