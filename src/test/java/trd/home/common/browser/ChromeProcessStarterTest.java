package trd.home.common.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChromeProcessStarterTest {
    @Test
    void startsConfiguredProcessWithoutLaunchingARealBrowser() throws Exception {
        var starter = new ChromeProcessStarter("chrome", "profile", 9333, List.of());
        try (var construction = org.mockito.Mockito.mockConstruction(ProcessBuilder.class, (builder, context) -> {
            assertEquals(List.of(starter.command()), context.arguments());
            org.mockito.Mockito.when(builder.inheritIO()).thenReturn(builder);
        })) {
            starter.start();
            assertEquals(1, construction.constructed().size());
            org.mockito.Mockito.verify(construction.constructed().getFirst()).start();
        }
    }

    @Test
    void wrapsProcessStartFailureAndPreservesCause() {
        var cause = new java.io.IOException("cannot start");
        try (var construction = org.mockito.Mockito.mockConstruction(ProcessBuilder.class, (builder, context) -> {
            org.mockito.Mockito.when(builder.inheritIO()).thenReturn(builder);
            org.mockito.Mockito.when(builder.start()).thenThrow(cause);
        })) {
            var exception = org.junit.jupiter.api.Assertions.assertThrows(
                    trd.home.common.exception.ChromeLaunchException.class,
                    () -> new ChromeProcessStarter("chrome", "profile", 9333, List.of()).start());
            org.junit.jupiter.api.Assertions.assertSame(cause, exception.getCause());
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
