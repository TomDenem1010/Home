package trd.home.common.browser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import trd.home.common.exception.ChromeLaunchException;

@Component
public class ChromeProcessStarter {

    private final String executablePath;
    private final String userDataDirectory;
    private final int remoteDebuggingPort;
    private final List<String> arguments;

    public ChromeProcessStarter(
            @Value("${chrome.executable-path}") String executablePath,
            @Value("${chrome.user-data-directory}") String userDataDirectory,
            @Value("${chrome.remote-debugging-port}") int remoteDebuggingPort,
            @Value("${chrome.arguments:}") List<String> arguments) {
        this.executablePath = executablePath;
        this.userDataDirectory = userDataDirectory;
        this.remoteDebuggingPort = remoteDebuggingPort;
        this.arguments = List.copyOf(arguments);
    }

    public void start() {
        try {
            new ProcessBuilder(command()).inheritIO().start();
        } catch (IOException exception) {
            throw new ChromeLaunchException("Unable to create Chrome process", exception);
        }
    }

    List<String> command() {
        List<String> command = new ArrayList<>();
        command.add(executablePath);
        command.add("--remote-debugging-port=" + remoteDebuggingPort);
        command.add("--user-data-dir=" + userDataDirectory);
        arguments.stream().filter(argument -> !argument.isBlank()).forEach(command::add);
        return command;
    }
}
