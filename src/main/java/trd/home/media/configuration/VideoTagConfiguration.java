package trd.home.media.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import trd.home.media.repository.VideoTagRepository;
import trd.home.media.validator.VideoTagNameLengthValidator;
import trd.home.media.validator.VideoTagNameRequiredValidator;
import trd.home.media.validator.VideoTagNameUniqueValidator;

@Configuration(proxyBeanMethods = false)
public class VideoTagConfiguration {
    @Bean
    @Order(1)
    VideoTagNameRequiredValidator videoTagNameRequiredValidator() {
        return new VideoTagNameRequiredValidator();
    }

    @Bean
    @Order(2)
    VideoTagNameLengthValidator videoTagNameLengthValidator() {
        return new VideoTagNameLengthValidator();
    }

    @Bean
    @Order(3)
    VideoTagNameUniqueValidator videoTagNameUniqueValidator(VideoTagRepository repository) {
        return new VideoTagNameUniqueValidator(repository);
    }
}
