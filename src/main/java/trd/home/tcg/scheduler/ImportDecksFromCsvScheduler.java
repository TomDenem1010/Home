package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ImportDecksFromCsvService;

@Component
@RequiredArgsConstructor
public class ImportDecksFromCsvScheduler {

    private final ApplicationEventQueue eventQueue;
    private final ImportDecksFromCsvService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.import-decks-from-csv.delay:10s}")
    public void processNextEvent() {
        eventQueue.claimNext(EventType.IMPORT_DECKS_FROM_CSV).ifPresent(event -> service.process(event));
    }
}
