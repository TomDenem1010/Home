package trd.home.media.exception;

import trd.home.common.exception.HomeException;

public class VideoTagNotFoundException extends HomeException {
    public VideoTagNotFoundException(String id) {
        super("Video tag is unavailable: " + id);
    }
}
