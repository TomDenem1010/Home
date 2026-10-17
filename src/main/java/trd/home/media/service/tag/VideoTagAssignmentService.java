package trd.home.media.service.tag;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;
import trd.home.media.dto.VideoTagDto;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.exception.VideoTagNotFoundException;
import trd.home.media.repository.VideoRepository;
import trd.home.media.repository.VideoTagRepository;

@Service
@RequiredArgsConstructor
public class VideoTagAssignmentService {
    private final VideoRepository videos;
    private final VideoTagRepository tags;

    @Transactional(readOnly = true)
    public List<VideoTagDto> tagsForVideo(String videoId) {
        var assignedIds =
                activeVideo(videoId).getTags().stream().map(tag -> tag.getId()).collect(Collectors.toSet());
        return tags.findAllByOrderByNameAsc().stream()
                .map(tag -> new VideoTagDto(tag.getId(), tag.getName(), assignedIds.contains(tag.getId())))
                .toList();
    }

    @Transactional
    public void setAssigned(String videoId, String tagId, boolean assigned) {
        var video = activeVideo(videoId);
        var tag = tags.findById(tagId).orElseThrow(() -> new VideoTagNotFoundException(tagId));
        boolean present =
                video.getTags().stream().anyMatch(existing -> existing.getId().equals(tagId));
        if (assigned && !present) {
            video.getTags().add(tag);
        } else if (!assigned) {
            video.getTags().removeIf(existing -> existing.getId().equals(tagId));
        }
    }

    private Video activeVideo(String id) {
        return videos.findByIdAndStatus(id, MediaStatus.ACTIVE)
                .orElseThrow(() -> new MediaVideoNotFoundException("Video is unavailable: " + id));
    }
}
