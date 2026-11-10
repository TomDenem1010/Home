package trd.home.boardgame.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import trd.home.boardgame.validator.*;

@Configuration(proxyBeanMethods = false)
public class BoardGameConfiguration {
    @Bean
    @Order(1)
    BoardGameNameValidator boardGameNameValidator() {
        return new BoardGameNameValidator();
    }

    @Bean
    @Order(2)
    BoardGameLinkValidator boardGameLinkValidator() {
        return new BoardGameLinkValidator();
    }

    @Bean
    @Order(3)
    BoardGameClassificationValidator boardGameClassificationValidator() {
        return new BoardGameClassificationValidator();
    }

    @Bean
    @Order(1)
    BoardGamePlayGameValidator boardGamePlayGameValidator() {
        return new BoardGamePlayGameValidator();
    }

    @Bean
    @Order(2)
    BoardGamePlayTimeValidator boardGamePlayTimeValidator() {
        return new BoardGamePlayTimeValidator();
    }

    @Bean
    @Order(3)
    BoardGamePlayRatingValidator boardGamePlayRatingValidator() {
        return new BoardGamePlayRatingValidator();
    }

    @Bean
    BoardGameSearchFilterValidator boardGameSearchFilterValidator() {
        return new BoardGameSearchFilterValidator();
    }

    @Bean
    BoardGamePlaySearchFilterValidator boardGamePlaySearchFilterValidator() {
        return new BoardGamePlaySearchFilterValidator();
    }
}
