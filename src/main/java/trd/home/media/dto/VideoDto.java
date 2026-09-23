package trd.home.media.dto;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Video;

public record VideoDto(
        @NonNull String id,
        @NonNull String name,
        @NonNull FolderDto folder,
        @NonNull Set<ActorDto> actors,
        @NonNull MediaStatus status) {
    @LogMethodCall
    public static VideoDto from(Video video) {
        return new VideoDto(
                Objects.requireNonNullElse(video.getId(), ""),
                Objects.requireNonNullElse(video.getName(), ""),
                FolderDto.from(video.getFolder()),
                video.getActors().stream().map(ActorDto::from).collect(Collectors.toUnmodifiableSet()),
                Objects.requireNonNullElse(video.getStatus(), MediaStatus.INACTIVE));
    }
}
