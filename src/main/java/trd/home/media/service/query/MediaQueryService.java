package trd.home.media.service.query;

import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dto.ActorDto;
import trd.home.media.dto.FolderDto;
import trd.home.media.dto.VideoDto;
import trd.home.media.exception.MediaVideoNotFoundException;
import trd.home.media.repository.ActorRepository;
import trd.home.media.repository.FolderRepository;
import trd.home.media.repository.VideoRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MediaQueryService {
    private final ActorRepository actors;
    private final FolderRepository folders;
    private final VideoRepository videos;

    public List<ActorDto> activeActors() {
        return actors.findByStatusOrderByName(MediaStatus.ACTIVE).stream()
                .map(ActorDto::from)
                .toList();
    }

    public List<FolderDto> activeFolders() {
        return folders.findByStatusOrderByPath(MediaStatus.ACTIVE).stream()
                .map(FolderDto::from)
                .toList();
    }

    public List<VideoDto> activeVideos() {
        return videos.findDistinctByStatusOrderByName(MediaStatus.ACTIVE).stream()
                .map(VideoDto::from)
                .toList();
    }

    public List<VideoDto> videosByActor(String id) {
        return videos.findDistinctByStatusAndActorsIdOrderByName(MediaStatus.ACTIVE, id).stream()
                .map(VideoDto::from)
                .toList();
    }

    public List<VideoDto> videosByFolder(String id) {
        return videos.findDistinctByStatusAndFolderIdOrderByName(MediaStatus.ACTIVE, id).stream()
                .map(VideoDto::from)
                .toList();
    }

    public Path videoPath(String id) {
        var video = videos.findByIdAndStatus(id, MediaStatus.ACTIVE)
                .orElseThrow(() -> new MediaVideoNotFoundException("Video is unavailable: " + id));
        Path folder = Path.of(video.getFolder().getPath()).toAbsolutePath().normalize();
        Path path = folder.resolve(video.getFileName()).normalize();
        if (!folder.equals(path.getParent())) {
            throw new MediaVideoNotFoundException("Video is unavailable: " + id);
        }
        return path;
    }
}
