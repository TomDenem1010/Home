package trd.home.media.validator;

import lombok.RequiredArgsConstructor;
import trd.home.media.exception.InvalidVideoTagException;
import trd.home.media.repository.VideoTagRepository;

@RequiredArgsConstructor
public class VideoTagNameUniqueValidator extends VideoTagNameValidator {
    private final VideoTagRepository repository;

    @Override
    public void validate(String name) {
        if (repository.existsByName(name)) {
            throw new InvalidVideoTagException("A video tag with this name already exists.");
        }
    }
}
