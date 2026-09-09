package trd.home.media.dto;

import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Folder;

public record FolderDto(String id, String path, String type, MediaStatus status) {
    @LogMethodCall
    public static FolderDto from(Folder folder) {
        return new FolderDto(folder.getId(), folder.getPath(), folder.getType(), folder.getStatus());
    }
}
