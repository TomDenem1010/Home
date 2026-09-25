package trd.home.tcg.validator;

import java.util.Objects;
import org.springframework.core.io.Resource;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.exception.WrongDeckNameException;

public class ResourceDeckNameValidator implements ResourceValidator {

    @Override
    public void validateResource(Resource resource) {
        String filename = resource.getFilename();

        checkCsvExtension(filename);
        checkNameAndVersionSeparator(filename);
        checkDeckName(filename);
        checkVersion(filename);
    }

    private void checkDeckName(String filename) {
        String deckName = filename.substring(0, filename.lastIndexOf('_'));
        if (deckName.isBlank() || deckName.contains("/") || deckName.contains("\\")) {
            throw new WrongDeckNameException("Deck filename must start with a valid deck name: name_version.csv");
        }
    }

    private void checkCsvExtension(String filename) {
        if (Objects.isNull(filename) || !filename.endsWith(".csv")) {
            throw new WrongDeckNameException("Deck resource must have a CSV filename: name_version.csv");
        }
    }

    private void checkNameAndVersionSeparator(String filename) {
        if (filename.chars().filter(character -> character == '_').count() != 1) {
            throw new WrongDeckNameException(
                    "Deck filename must contain a name and version separated by an underscore and nothing else: name_version.csv");
        }
    }

    private void checkVersion(String filename) {
        int versionSeparator = filename.lastIndexOf('_');
        String version = filename.substring(versionSeparator + 1, filename.length() - ".csv".length());
        if (!version.matches("[vV]?\\d+")) {
            throw new WrongDeckNameException(
                    "Deck filename version must be numeric with an optional v prefix: name_version.csv");
        }
    }
}
