package trd.home.media.dto;

import java.util.Objects;
import lombok.NonNull;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Folder;

public record FolderDto(
        @NonNull String id,
        @NonNull String path,
        @NonNull String type,
        @NonNull MediaStatus status) {
    public static FolderDto from(Folder folder) {
        return new FolderDto(
                Objects.requireNonNullElse(folder.getId(), ""),
                Objects.requireNonNullElse(folder.getPath(), ""),
                Objects.requireNonNullElse(folder.getType(), ""),
                Objects.requireNonNullElse(folder.getStatus(), MediaStatus.INACTIVE));
    }
}
