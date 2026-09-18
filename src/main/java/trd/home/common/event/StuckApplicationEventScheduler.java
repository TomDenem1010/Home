package trd.home.common.event;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StuckApplicationEventScheduler {

    private final StuckApplicationEventService eventService;

    public StuckApplicationEventScheduler(StuckApplicationEventService eventService) {
        this.eventService = eventService;
    }

    @Scheduled(fixedDelayString = "${event.stuck.scheduler.delay:1m}")
    public void failStuckEvents() {
        eventService.failStuckEvents();
    }
}
