package trd.home.tcg.configuration;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import trd.home.tcg.service.file.DeckCsvFileReader;
import trd.home.tcg.service.file.FileRouterService;
import trd.home.tcg.validator.CardSearchPagingValidator;
import trd.home.tcg.validator.CardSearchPriceValidator;
import trd.home.tcg.validator.CsvDeckCardValidator;
import trd.home.tcg.validator.CsvDeckEncodingValidator;
import trd.home.tcg.validator.CsvDeckNameValidator;

@Configuration(proxyBeanMethods = false)
public class TcgConfiguration {

    @Bean
    CardSearchPagingValidator cardSearchPagingValidator() {
        return new CardSearchPagingValidator();
    }

    @Bean
    CardSearchPriceValidator cardSearchPriceValidator() {
        return new CardSearchPriceValidator();
    }

    @Bean
    CsvDeckEncodingValidator csvDeckEncodingValidator() {
        return new CsvDeckEncodingValidator();
    }

    @Bean
    CsvDeckNameValidator csvDeckNameValidator() {
        return new CsvDeckNameValidator();
    }

    @Bean
    CsvDeckCardValidator csvDeckCardValidator() {
        return new CsvDeckCardValidator();
    }

    @Bean
    DeckCsvFileReader deckCsvFileReader(
            CsvDeckEncodingValidator csvDeckEncodingValidator,
            CsvDeckNameValidator csvDeckNameValidator,
            CsvDeckCardValidator csvDeckCardValidator,
            FileRouterService fileRouterService) {
        return new DeckCsvFileReader(
                fileRouterService, List.of(csvDeckEncodingValidator, csvDeckNameValidator, csvDeckCardValidator));
    }
}
