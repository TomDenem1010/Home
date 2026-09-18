package trd.home.tcg.service;

import java.io.IOException;
import org.springframework.stereotype.Component;
import trd.home.tcg.exception.ChromeLaunchException;

@Component
public class ChromeProcessStarter {

    private static final String CHROME_EXECUTABLE = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String REMOTE_DEBUGGING_PORT = "--remote-debugging-port=9222";
    private static final String USER_DATA_DIRECTORY = "--user-data-dir=C:\\ChromeProfile";

    public void start() {
        try {
            new ProcessBuilder(CHROME_EXECUTABLE, REMOTE_DEBUGGING_PORT, USER_DATA_DIRECTORY).start();
        } catch (IOException exception) {
            throw new ChromeLaunchException("Unable to create Chrome process", exception);
        }
    }
}
