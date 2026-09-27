package trd.home.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import trd.home.auth.validator.ApplicationLogSearchPagingValidator;
import trd.home.auth.validator.ApplicationLogSearchTimeValidator;

@Configuration(proxyBeanMethods = false)
public class ApplicationLogSearchConfiguration {
    @Bean
    ApplicationLogSearchPagingValidator applicationLogSearchPagingValidator() {
        return new ApplicationLogSearchPagingValidator();
    }

    @Bean
    ApplicationLogSearchTimeValidator applicationLogSearchTimeValidator() {
        return new ApplicationLogSearchTimeValidator();
    }
}
