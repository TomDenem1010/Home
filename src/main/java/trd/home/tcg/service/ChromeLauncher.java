package trd.home.tcg.service;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.exception.ChromeLaunchException;

@Slf4j
@Service
public class ChromeLauncher {

    private static final String CHROME_EXECUTABLE = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String REMOTE_DEBUGGING_PORT = "--remote-debugging-port=9222";
    private static final String USER_DATA_DIRECTORY = "--user-data-dir=C:\\ChromeProfile";

    @LogMethodCall
    public void start() {
        try {
            new ProcessBuilder(CHROME_EXECUTABLE, REMOTE_DEBUGGING_PORT, USER_DATA_DIRECTORY).start();
        } catch (IOException exception) {
            log.error("Failed to start Chrome for Cardmarket price collection", exception);
            throw new ChromeLaunchException("Unable to start Chrome", exception);
        }
    }
}
