package trd.home.common.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import trd.home.common.exception.ChromeLaunchException;

class ChromeProcessStarterTest {
    @Test
    void startsConfiguredProcessWithoutLaunchingARealBrowser() throws Exception {
        var starter = new ChromeProcessStarter("chrome", "profile", 9333, List.of());
        try (var construction = Mockito.mockConstruction(ProcessBuilder.class, (builder, context) -> {
            assertEquals(List.of(starter.command()), context.arguments());
            Mockito.when(builder.inheritIO()).thenReturn(builder);
        })) {
            starter.start();
            assertEquals(1, construction.constructed().size());
            Mockito.verify(construction.constructed().getFirst()).start();
        }
    }

    @Test
    void wrapsProcessStartFailureAndPreservesCause() {
        var cause = new IOException("cannot start");
        try (var construction = Mockito.mockConstruction(ProcessBuilder.class, (builder, context) -> {
            Mockito.when(builder.inheritIO()).thenReturn(builder);
            Mockito.when(builder.start()).thenThrow(cause);
        })) {
            var exception = Assertions.assertThrows(
                    ChromeLaunchException.class,
                    () -> new ChromeProcessStarter("chrome", "profile", 9333, List.of()).start());
            Assertions.assertSame(cause, exception.getCause());
            assertEquals(1, construction.constructed().size());
        }
    }

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
