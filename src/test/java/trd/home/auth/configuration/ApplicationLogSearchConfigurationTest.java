package trd.home.auth.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ApplicationLogSearchConfigurationTest {
    @Test
    void createsSearchValidators() {
        var configuration = new ApplicationLogSearchConfiguration();
        assertNotNull(configuration.applicationLogSearchPagingValidator());
        assertNotNull(configuration.applicationLogSearchTimeValidator());
    }
}
