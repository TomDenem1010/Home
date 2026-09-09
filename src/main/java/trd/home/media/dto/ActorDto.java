package trd.home.media.dto;

import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Actor;

public record ActorDto(String id, String name, MediaStatus status) {
    @LogMethodCall
    public static ActorDto from(Actor actor) {
        return new ActorDto(actor.getId(), actor.getName(), actor.getStatus());
    }
}
