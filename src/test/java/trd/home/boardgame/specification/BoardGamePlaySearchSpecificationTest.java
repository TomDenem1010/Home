package trd.home.boardgame.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.boardgame.dao.*;
import trd.home.boardgame.dto.BoardGamePlaySearchFilter;

class BoardGamePlaySearchSpecificationTest {
    private final Root<BoardGamePlay> root = mock();
    private final Path<BoardGame> game = mock();
    private final Path<String> name = mock();
    private final Path<LocalDateTime> time = mock();
    private final Path<Integer> rating = mock();
    private final CriteriaBuilder builder = mock();

    @Test
    void combinesGameNameAndInclusiveTimeAndRatingRanges() {
        when(root.<BoardGame>get("boardGame")).thenReturn(game);
        when(game.<String>get("name")).thenReturn(name);
        when(root.<LocalDateTime>get("time")).thenReturn(time);
        when(root.<Integer>get("rating")).thenReturn(rating);
        when(builder.lower(name)).thenReturn(name);
        var from = LocalDateTime.of(2026, 11, 1, 0, 0);
        var to = from.plusDays(10);
        BoardGamePlaySearchSpecification.matching(new BoardGamePlaySearchFilter(" A%_\\Z ", from, to, 2, 5))
                .toPredicate(root, null, builder);
        verify(builder).like(name, "%a\\%\\_\\\\z%", '\\');
        verify(builder).greaterThanOrEqualTo(time, from);
        verify(builder).lessThanOrEqualTo(time, to);
        verify(builder).ge(rating, 2);
        verify(builder).le(rating, 5);
        var predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicates.capture());
        assertEquals(5, predicates.getValue().length);
    }

    @Test
    void leavesEmptySearchUnrestricted() {
        BoardGamePlaySearchSpecification.matching(new BoardGamePlaySearchFilter(null, null, null, null, null))
                .toPredicate(root, null, builder);
        verifyNoInteractions(root);
        verify(builder).and(new Predicate[0]);
    }
}
