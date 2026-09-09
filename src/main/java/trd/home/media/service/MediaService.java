package trd.home.media.service;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.*;
import trd.home.media.exception.*;

@Service
public class MediaService {
    private final MediaQueryService query;
    private final MediaImportService importer;

    public MediaService(MediaQueryService query, MediaImportService importer) {
        this.query = query;
        this.importer = importer;
    }

    @LogMethodCall
    public int importPath(String path) {
        return importer.importPath(path);
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
    public List<VideoDto> activeVideos() {
        return query.activeVideos();
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
        var path = query.videoPath(id);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path)) {
            throw new MediaVideoNotFoundException("Video is unavailable: " + id);
        }
        return new FileSystemResource(path);
    }
}
