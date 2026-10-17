package trd.home.media.service.tag;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.media.dao.VideoTag;
import trd.home.media.dto.VideoTagSummaryDto;
import trd.home.media.repository.VideoTagRepository;
import trd.home.media.validator.VideoTagNameValidator;

@Service
@RequiredArgsConstructor
public class VideoTagService {
    private final VideoTagRepository repository;
    private final List<VideoTagNameValidator> validators;

    @Transactional(readOnly = true)
    public List<VideoTagSummaryDto> allTags() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(tag -> new VideoTagSummaryDto(tag.getId(), tag.getName()))
                .toList();
    }

    @Transactional
    public void create(String name) {
        String normalizedName =
                name == null ? null : name.toUpperCase(Locale.ROOT).strip();
        validators.forEach(validator -> validator.validate(normalizedName));
        var tag = new VideoTag();
        tag.setName(normalizedName);
        repository.save(tag);
    }
}
