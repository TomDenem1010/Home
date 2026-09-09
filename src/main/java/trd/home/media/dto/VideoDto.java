package trd.home.media.dto;

import java.util.Set;
import java.util.stream.Collectors;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;

public record VideoDto(String id, String name, FolderDto folder, Set<ActorDto> actors, MediaStatus status) {
    @LogMethodCall
    public static VideoDto from(Video video) {
        return new VideoDto(
                video.getId(),
                video.getName(),
                FolderDto.from(video.getFolder()),
                video.getActors().stream().map(ActorDto::from).collect(Collectors.toUnmodifiableSet()),
                video.getStatus());
    }
}
