package trd.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import trd.home.common.logging.LogMethodCall;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class HomeApplication {

    @LogMethodCall
    public static void main(String[] args) {
        SpringApplication.run(HomeApplication.class, args);
    }
}
