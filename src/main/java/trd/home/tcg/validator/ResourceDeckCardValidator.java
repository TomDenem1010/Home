package trd.home.tcg.validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.exception.UnableToReadResourcesException;
import trd.home.tcg.exception.WrongCardLineException;

@Slf4j
public class ResourceDeckCardValidator implements ResourceValidator {

    @Override
    public void validateResource(Resource resource) {
        try (var reader = resource.getInputStream()) {
            AtomicInteger lineNumber = new AtomicInteger();
            new String(reader.readAllBytes(), StandardCharsets.UTF_8).lines().forEach(line -> {
                int currentLineNumber = lineNumber.incrementAndGet();
                if (!line.isBlank()) {
                    checkCardLine(line, currentLineNumber);
                }
            });
        } catch (IOException exception) {
            log.error(
                    "Failed to read deck file '{}' while validating its card entries",
                    resource.getFilename(),
                    exception);
            throw new UnableToReadResourcesException("Unable to read deck file: " + resource.getFilename(), exception);
        }
    }

    private void checkCardLine(String line, int lineNumber) {
        String[] values = line.split(",", -1);

        if (values.length != 3) {
            throw new WrongCardLineException(
                    "Deck line " + lineNumber + " must contain quantity, link and foil type: " + line);
        }

        if (!values[0].matches("[1-9]\\d*")) {
            throw new WrongCardLineException("Deck line " + lineNumber + " must contain a valid quantity: " + line);
        }

        if (!values[1].matches("https://www.cardmarket.com/.+")) {
            throw new WrongCardLineException("Deck line " + lineNumber + " must contain a valid link: " + line);
        }

        if (values[2].isBlank() || CardFoilType.fromString(values[2]) == null) {
            throw new WrongCardLineException("Deck line " + lineNumber + " must contain a valid foil type: " + line);
        }
    }
}
