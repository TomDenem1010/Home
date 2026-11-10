package trd.home.boardgame.validator;

import java.net.URI;
import trd.home.boardgame.dto.BoardGameInput;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGameLinkValidator implements BoardGameInputValidator {
    @Override
    public void validate(BoardGameInput input) {
        String link = input.bggLink();
        if (link == null || link.isBlank()) return;
        try {
            URI uri = URI.create(link);
            if (link.length() > 1000
                    || !"https".equals(uri.getScheme())
                    || !("boardgamegeek.com".equals(uri.getHost()) || "www.boardgamegeek.com".equals(uri.getHost()))
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || uri.getPath() == null
                    || !uri.getPath().matches("/boardgame(?:expansion)?/[1-9][0-9]*(?:/[^/]+)?/?")) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidBoardGameException("Enter a valid HTTPS BoardGameGeek game URL.");
        }
    }
}
