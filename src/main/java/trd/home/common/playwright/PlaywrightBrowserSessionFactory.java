package trd.home.common.playwright;

import org.springframework.stereotype.Component;

@Component
public class PlaywrightBrowserSessionFactory {

    public PlaywrightBrowserSession open(String endpoint) {
        return new PlaywrightBrowserSession(endpoint);
    }
}
