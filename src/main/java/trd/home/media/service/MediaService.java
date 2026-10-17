package trd.home.media.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.dto.ActorDto;
import trd.home.media.dto.FolderDto;
import trd.home.media.dto.VideoDto;
import trd.home.media.dto.VideoTagDto;
import trd.home.media.dto.VideoTagSummaryDto;
import trd.home.media.service.application.MediaCommandService;
import trd.home.media.service.query.MediaQueryService;
import trd.home.media.service.tag.VideoTagAssignmentService;
import trd.home.media.service.tag.VideoTagService;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaQueryService query;
    private final MediaCommandService commands;
    private final VideoTagService videoTags;
    private final VideoTagAssignmentService tagAssignments;

    public List<VideoTagDto> videoTags(String videoId) {
        return tagAssignments.tagsForVideo(videoId);
    }

    public void setVideoTagAssigned(String videoId, String tagId, boolean assigned) {
        tagAssignments.setAssigned(videoId, tagId, assigned);
    }

    @LogMethodCall
    public void createVideoTag(String name) {
        videoTags.create(name);
    }

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

    @LogMethodCall
    public List<VideoTagSummaryDto> allVideoTags() {
        return videoTags.allTags();
    }

    @LogMethodCall
    public List<VideoDto> videosByTag(String id) {
        return query.videosByTag(id);
    }

    public Resource videoResource(String id) {
        return commands.videoResource(id);
    }
}
