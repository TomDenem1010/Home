package trd.home.tcg.configuration;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import trd.home.tcg.service.file.DeckFileReader;
import trd.home.tcg.validator.ResourceDeckCardValidator;
import trd.home.tcg.validator.ResourceDeckEncodingValidator;
import trd.home.tcg.validator.ResourceDeckNameValidator;

@Configuration(proxyBeanMethods = false)
public class TcgConfiguration {

    @Bean
    ResourceDeckEncodingValidator resourceDeckEncodingValidator() {
        return new ResourceDeckEncodingValidator();
    }

    @Bean
    ResourceDeckNameValidator resourceDeckNameValidator() {
        return new ResourceDeckNameValidator();
    }

    @Bean
    ResourceDeckCardValidator resourceDeckCardValidator() {
        return new ResourceDeckCardValidator();
    }

    @Bean
    DeckFileReader deckFileReader(
            ResourceDeckEncodingValidator resourceDeckEncodingValidator,
            ResourceDeckNameValidator resourceDeckNameValidator,
            ResourceDeckCardValidator resourceDeckCardValidator) {
        return new DeckFileReader(
                List.of(resourceDeckEncodingValidator, resourceDeckNameValidator, resourceDeckCardValidator));
    }
}
