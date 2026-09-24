package trd.home.tcg.validator;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import trd.home.common.exception.ResourceReadException;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.exception.WrongDeckEncodingException;

@Slf4j
public class ResourceDeckEncodingValidator implements ResourceValidator {

    @Override
    public void validateResource(Resource resource) {
        try (var input = resource.getInputStream()) {
            createUtf8Decoder().decode(ByteBuffer.wrap(input.readAllBytes()));
        } catch (CharacterCodingException exception) {
            log.error("Deck file '{}' is not valid UTF-8", resource.getFilename(), exception);
            throw new WrongDeckEncodingException("Deck file is not valid UTF-8.");
        } catch (Exception exception) {
            log.error("Failed to read deck file '{}' while validating its encoding", resource.getFilename(), exception);
            throw new ResourceReadException("Unable to read deck file: " + resource.getFilename(), exception);
        }
    }

    private CharsetDecoder createUtf8Decoder() {
        return StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
    }
}
