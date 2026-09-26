package trd.home.common.browser;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import trd.home.common.exception.ChromeLaunchException;

@Component
public class ChromeProcessStarter {

    private static final String REMOTE_DEBUGGING_PORT = "--remote-debugging-port=9222";

    private final String executablePath;
    private final String userDataDirectory;

    public ChromeProcessStarter(
            @Value("${chrome.executable-path}") String executablePath,
            @Value("${chrome.user-data-directory}") String userDataDirectory) {
        this.executablePath = executablePath;
        this.userDataDirectory = userDataDirectory;
    }

    public void start() {
        try {
            new ProcessBuilder(executablePath, REMOTE_DEBUGGING_PORT, "--user-data-dir=" + userDataDirectory).start();
        } catch (IOException exception) {
            throw new ChromeLaunchException("Unable to create Chrome process", exception);
        }
    }
}
