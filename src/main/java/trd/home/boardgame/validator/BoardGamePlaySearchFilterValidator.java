package trd.home.boardgame.validator;

import java.time.LocalDateTime;
import trd.home.boardgame.dto.BoardGamePlaySearchFilter;
import trd.home.boardgame.exception.InvalidBoardGameException;

public class BoardGamePlaySearchFilterValidator implements BoardGamePlaySearchValidator {
    @Override
    public void validate(BoardGamePlaySearchFilter filter, int page) {
        if (page < 0 || (filter.name() != null && filter.name().length() > 255))
            throw new InvalidBoardGameException(
                    "Invalid play search: page must be nonnegative and name at most 255 characters.");
        if (invalidDate(filter.from())
                || invalidDate(filter.to())
                || (filter.from() != null
                        && filter.to() != null
                        && filter.from().isAfter(filter.to())))
            throw new InvalidBoardGameException("Enter a valid, ordered date range.");
        if (invalidRating(filter.ratingMin())
                || invalidRating(filter.ratingMax())
                || (filter.ratingMin() != null
                        && filter.ratingMax() != null
                        && filter.ratingMin() > filter.ratingMax()))
            throw new InvalidBoardGameException("Enter an ordered rating range between 1 and 5.");
    }

    private boolean invalidDate(LocalDateTime time) {
        return time != null && (time.getYear() < 1 || time.getYear() > 9999);
    }

    private boolean invalidRating(Integer rating) {
        return rating != null && (rating < 1 || rating > 5);
    }
}
