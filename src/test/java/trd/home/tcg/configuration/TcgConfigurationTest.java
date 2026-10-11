package trd.home.tcg.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TcgConfigurationTest {

    private final TcgConfiguration configuration = new TcgConfiguration();

    @Test
    void createsTcgBeans() {
        assertNotNull(configuration.cardSearchPagingValidator());
        assertNotNull(configuration.cardSearchPriceValidator());
        var encodingValidator = configuration.csvDeckEncodingValidator();
        var nameValidator = configuration.csvDeckNameValidator();
        var cardValidator = configuration.csvDeckCardValidator();

        assertNotNull(encodingValidator);
        assertNotNull(nameValidator);
        assertNotNull(cardValidator);
        assertNotNull(configuration.deckCsvFileReader(
                encodingValidator, nameValidator, cardValidator, "classpath*:tcg/deck/*.csv"));
    }
}
