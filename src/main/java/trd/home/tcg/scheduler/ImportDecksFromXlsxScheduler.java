package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ImportDecksFromXlsxService;

@Component
@RequiredArgsConstructor
public class ImportDecksFromXlsxScheduler {
    private final ApplicationEventQueue events;
    private final ImportDecksFromXlsxService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.import-decks-from-xlsx.delay:10s}")
    public void processNextEvent() {
        events.claimNext(EventType.IMPORT_DECKS_FROM_XLSX).ifPresent(service::process);
    }
}
