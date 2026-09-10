package trd.home.media.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.service.MediaImportService;

class MediaImportSchedulerTest {
    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final MediaImportService mediaImportService = mock(MediaImportService.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final MediaImportScheduler scheduler =
            new MediaImportScheduler(eventRepository, mediaImportService, notificationPublisher);

    @Test
    void processesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Media");
        List<EventStatus> savedStatuses = new ArrayList<>();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(mediaImportService.importPath("C:\\Media")).thenReturn(12);
        doAnswer(invocation -> {
                    savedStatuses.add(event.getStatus());
                    return event;
                })
                .when(eventRepository)
                .save(event);

        scheduler.processNextEvent();

        InOrder order = inOrder(eventRepository, mediaImportService);
        order.verify(eventRepository).save(event);
        order.verify(mediaImportService).importPath("C:\\Media");
        order.verify(eventRepository).save(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        assertEquals(List.of(EventStatus.PROCESSING, EventStatus.DONE), savedStatuses);
        verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Imported videos: 12");
    }

    @Test
    void storesErrorWhenImportFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Missing");
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.IMPORT_MEDIA, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(mediaImportService.importPath("C:\\Missing"))
                .thenThrow(new IllegalStateException("Directory is unavailable"));

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Directory is unavailable", event.getErrorMessage());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.ERROR, "Failed to import media: Directory is unavailable");
        InOrder order = inOrder(eventRepository, mediaImportService);
        order.verify(eventRepository).save(event);
        order.verify(mediaImportService).importPath("C:\\Missing");
        order.verify(eventRepository).save(event);
    }
}
