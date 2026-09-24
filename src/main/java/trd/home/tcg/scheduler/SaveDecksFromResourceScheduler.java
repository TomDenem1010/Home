package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.SaveDecksFromResourceService;

@Component
@RequiredArgsConstructor
public class SaveDecksFromResourceScheduler {

    private final ApplicationEventQueue eventQueue;
    private final SaveDecksFromResourceService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.save-decks.delay:5s}")
    public void processNextEvent() {
        eventQueue.claimNext(EventType.SAVE_DECKS_FROM_RESOURCE).ifPresent(event -> service.process(event));
    }
}
