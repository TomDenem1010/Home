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
    }

    private void checkCsvExtension(String filename) {
        if (Objects.isNull(filename) || !filename.endsWith(".csv")) {
            throw new WrongDeckNameException("Deck resource must have a CSV filename: name_version.csv");
        }
    }

    private void checkNameAndVersionSeparator(String filename) {
        int versionSeparator = filename.lastIndexOf('_');

        if (versionSeparator != 1) {
            throw new WrongDeckNameException(
                    "Deck filename must contain a name and version separated by an underscore and nothing else: name_version.csv");
        }
    }
}
