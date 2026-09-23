package trd.home.media.dto;

import java.util.Objects;
import lombok.NonNull;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Actor;

public record ActorDto(
        @NonNull String id, @NonNull String name, @NonNull MediaStatus status) {
    @LogMethodCall
    public static ActorDto from(Actor actor) {
        return new ActorDto(
                Objects.requireNonNullElse(actor.getId(), ""),
                Objects.requireNonNullElse(actor.getName(), ""),
                Objects.requireNonNullElse(actor.getStatus(), MediaStatus.INACTIVE));
    }
}
