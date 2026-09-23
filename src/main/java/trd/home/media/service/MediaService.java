package trd.home.media.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.ActorDto;
import trd.home.media.dto.FolderDto;
import trd.home.media.dto.VideoDto;
import trd.home.media.service.application.MediaCommandService;
import trd.home.media.service.query.MediaQueryService;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaQueryService query;
    private final MediaCommandService commands;

    @LogMethodCall
    public void importPath(String path) {
        commands.importPath(path);
    }

    @LogMethodCall
    public List<ActorDto> activeActors() {
        return query.activeActors();
    }

    @LogMethodCall
    public List<FolderDto> activeFolders() {
        return query.activeFolders();
    }

    @LogMethodCall
    public List<VideoDto> videosByActor(String id) {
        return query.videosByActor(id);
    }

    @LogMethodCall
    public List<VideoDto> videosByFolder(String id) {
        return query.videosByFolder(id);
    }

    public Resource videoResource(String id) {
        return commands.videoResource(id);
    }
}
