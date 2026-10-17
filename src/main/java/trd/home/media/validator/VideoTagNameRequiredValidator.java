package trd.home.media.validator;

import trd.home.media.exception.InvalidVideoTagException;

public class VideoTagNameRequiredValidator extends VideoTagNameValidator {
    @Override
    public void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidVideoTagException("Video tag name is required.");
        }
    }
}
