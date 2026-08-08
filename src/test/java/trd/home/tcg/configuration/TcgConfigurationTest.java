package trd.home.tcg.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TcgConfigurationTest {

    private final TcgConfiguration configuration = new TcgConfiguration();

    @Test
    void createsTcgBeans() {
        var encodingValidator = configuration.resourceDeckEncodingValidator();
        var nameValidator = configuration.resourceDeckNameValidator();
        var cardValidator = configuration.resourceDeckCardValidator();

        assertNotNull(encodingValidator);
        assertNotNull(nameValidator);
        assertNotNull(cardValidator);
        assertNotNull(configuration.deckFileReader(encodingValidator, nameValidator, cardValidator));
    }
}
