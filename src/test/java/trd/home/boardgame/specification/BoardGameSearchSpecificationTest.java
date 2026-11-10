package trd.home.boardgame.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.boardgame.constant.*;
import trd.home.boardgame.dao.BoardGame;
import trd.home.boardgame.dto.BoardGameSearchFilter;

class BoardGameSearchSpecificationTest {
    private final Root<BoardGame> root = mock();
    private final Path<String> name = mock();
    private final Path<BoardGameState> state = mock();
    private final Path<BoardGameType> type = mock();
    private final CriteriaBuilder builder = mock();

    @Test
    void combinesLiteralCaseInsensitiveNameWithStateAndType() {
        when(root.<String>get("name")).thenReturn(name);
        when(root.<BoardGameState>get("state")).thenReturn(state);
        when(root.<BoardGameType>get("type")).thenReturn(type);
        when(builder.lower(name)).thenReturn(name);
        var result = mock(Predicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(result);
        assertSame(
                result,
                BoardGameSearchSpecification.matching(
                                new BoardGameSearchFilter(" A%_\\Z ", BoardGameState.SOLD, BoardGameType.EXPANSION))
                        .toPredicate(root, null, builder));
        verify(builder).like(name, "%a\\%\\_\\\\z%", '\\');
        verify(builder).equal(state, BoardGameState.SOLD);
        verify(builder).equal(type, BoardGameType.EXPANSION);
        var predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicates.capture());
        assertEquals(3, predicates.getValue().length);
    }

    @Test
    void leavesUnspecifiedFiltersUnrestricted() {
        for (String text : new String[] {null, "", "  "}) {
            BoardGameSearchSpecification.matching(new BoardGameSearchFilter(text, null, null))
                    .toPredicate(root, null, builder);
        }
        verifyNoInteractions(root);
        verify(builder, times(3)).and(new Predicate[0]);
    }
}
