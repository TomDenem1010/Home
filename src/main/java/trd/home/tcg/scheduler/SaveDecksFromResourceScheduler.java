package trd.home.tcg.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.service.SaveDecksFromResourceService;

@Component
@RequiredArgsConstructor
public class SaveDecksFromResourceScheduler {

    private final ApplicationEventRepository eventRepository;
    private final SaveDecksFromResourceService service;

    @Scheduled(fixedDelayString = "${tcg.scheduler.save-decks.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO)
                .ifPresent(service::process);
    }
}
