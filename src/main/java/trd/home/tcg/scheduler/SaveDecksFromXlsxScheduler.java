package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.tcg.service.event.SaveDecksFromXlsxService;

@Component
@RequiredArgsConstructor
public class SaveDecksFromXlsxScheduler {
    private final ApplicationEventQueue events;
    private final SaveDecksFromXlsxService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.import-decks-from-xlsx.delay:10s}")
    public void processNextEvent() {
        events.claimNext(EventType.SAVE_DECKS_FROM_XLSX).ifPresent(service::process);
    }
}
