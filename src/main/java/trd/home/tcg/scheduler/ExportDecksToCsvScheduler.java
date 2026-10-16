package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ExportDecksToCsvService;

@Component
@RequiredArgsConstructor
public class ExportDecksToCsvScheduler {
    private final ApplicationEventQueue events;
    private final ExportDecksToCsvService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.export-decks-to-csv.delay:10s}")
    public void processNextEvent() {
        events.claimNext(EventType.EXPORT_DECKS_TO_CSV).ifPresent(service::process);
    }
}
