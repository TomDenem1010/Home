package trd.home.media.validator;

import trd.home.media.exception.InvalidVideoTagException;

public class VideoTagNameLengthValidator extends VideoTagNameValidator {
    @Override
    public void validate(String name) {
        if (name.length() > 255) {
            throw new InvalidVideoTagException("Video tag name must not exceed 255 characters.");
        }
    }
}
