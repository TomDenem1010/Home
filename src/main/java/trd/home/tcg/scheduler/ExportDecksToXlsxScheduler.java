package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.ExportDecksToXlsxService;

@Component
@RequiredArgsConstructor
public class ExportDecksToXlsxScheduler {
    private final ApplicationEventQueue events;
    private final ExportDecksToXlsxService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.export-decks-to-xlsx.delay:10s}")
    public void processNextEvent() {
        events.claimNext(EventType.EXPORT_DECKS_TO_XLSX).ifPresent(service::process);
    }
}
