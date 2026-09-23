package trd.home.media.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.media.service.importing.MediaImportService;

@Service
@RequiredArgsConstructor
public class MediaImportEventProcessor {

    private final ApplicationEventProcessor eventProcessor;
    private final MediaImportService importService;

    public void process(ApplicationEvent event) {
        eventProcessor.process(
                event,
                () -> "Imported videos: " + importService.importPath(event.getMessage()),
                "Failed to import media: ");
    }
}
