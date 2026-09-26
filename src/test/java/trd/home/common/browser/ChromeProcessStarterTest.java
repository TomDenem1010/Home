package trd.home.common.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChromeProcessStarterTest {
    @Test
    void buildsCommandWithConfiguredPortAndSeparateArguments() {
        var starter = new ChromeProcessStarter(
                "/opt/chrome browser/chrome",
                "/data/chrome profile",
                9333,
                List.of("--headless=new", "--remote-debugging-address=0.0.0.0", ""));

        assertEquals(
                List.of(
                        "/opt/chrome browser/chrome",
                        "--remote-debugging-port=9333",
                        "--user-data-dir=/data/chrome profile",
                        "--headless=new",
                        "--remote-debugging-address=0.0.0.0"),
                starter.command());
    }
}
