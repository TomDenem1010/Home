package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.media.exception.InvalidMediaPathException;

class MediaImportEventProcessorTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final MediaImportService importService = mock(MediaImportService.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final MediaImportEventProcessor processor =
            new MediaImportEventProcessor(eventRepository, importService, notificationPublisher);

    @Test
    void completesSuccessfulImport() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Media");
        when(importService.importPath("C:\\Media")).thenReturn(12);

        processor.process(event);

        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Imported videos: 12");
        InOrder order = inOrder(eventRepository, importService);
        order.verify(eventRepository).save(event);
        order.verify(importService).importPath("C:\\Media");
        order.verify(eventRepository).save(event);
    }

    @Test
    void storesImportFailure() {
        ApplicationEvent event = new ApplicationEvent(EventType.IMPORT_MEDIA, "C:\\Missing");
        when(importService.importPath("C:\\Missing"))
                .thenThrow(new InvalidMediaPathException("Directory is unavailable"));

        processor.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Directory is unavailable", event.getErrorMessage());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.ERROR, "Failed to import media: Directory is unavailable");
    }
}
