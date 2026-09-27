package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import trd.home.auth.constant.ApplicationLogSort;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.dto.ApplicationLogSearchResult;
import trd.home.auth.dto.UserDto;
import trd.home.auth.exception.InvalidApplicationLogSearchException;
import trd.home.auth.service.user.AuthOperationsService;
import trd.home.auth.validator.ApplicationLogSearchFilterValidator;
import trd.home.auth.validator.ApplicationLogSearchPagingValidator;
import trd.home.auth.validator.ApplicationLogSearchTimeValidator;

class AuthServiceTest {
    @Test
    void listsExistingCreatorsAndIncludesSystemOnce() {
        var operations = mock(AuthOperationsService.class);
        var service = new AuthService(operations, mock(ApplicationLogSearchService.class), List.of());
        when(operations.getAllUsers())
                .thenReturn(List.of(
                        new UserDto("1", "bob", Set.of()),
                        new UserDto("2", "alice", Set.of()),
                        new UserDto("3", "system", Set.of())));
        assertEquals(List.of("alice", "bob", "system"), service.getApplicationLogCreators());
        when(operations.getAllUsers()).thenReturn(List.of());
        assertEquals(List.of("system"), service.getApplicationLogCreators());
    }

    @Test
    void delegatesLogFiltersAndPagingToSearchService() {
        var operations = mock(AuthOperationsService.class);
        var searchService = mock(ApplicationLogSearchService.class);
        var first = mock(ApplicationLogSearchFilterValidator.class);
        var second = mock(ApplicationLogSearchFilterValidator.class);
        var service = new AuthService(operations, searchService, List.of(first, second));
        var filter = new ApplicationLogSearchFilter("save", "alice", null, null, true);
        var results = new PageImpl<ApplicationLogSearchResult>(List.of());
        when(searchService.search(filter, 2, 25, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT))
                .thenReturn(results);

        assertSame(
                results,
                service.searchApplicationLogs(filter, 2, 25, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT));
        var order = inOrder(first, second, searchService);
        order.verify(first).validate(filter, 2, 25);
        order.verify(second).validate(filter, 2, 25);
        order.verify(searchService).search(filter, 2, 25, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT);
        verifyNoInteractions(operations);
    }

    @Test
    void rejectsInvalidPagingBeforeCallingSearchService() {
        var searchService = mock(ApplicationLogSearchService.class);
        var service = new AuthService(
                mock(AuthOperationsService.class), searchService, List.of(new ApplicationLogSearchPagingValidator()));
        var filter = new ApplicationLogSearchFilter(null, null, null, null, null);
        assertThrows(
                InvalidApplicationLogSearchException.class,
                () -> service.searchApplicationLogs(
                        filter, -1, 50, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT));
        assertThrows(
                InvalidApplicationLogSearchException.class,
                () -> service.searchApplicationLogs(filter, 0, 0, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT));
        assertThrows(
                InvalidApplicationLogSearchException.class,
                () -> service.searchApplicationLogs(
                        filter, 0, 201, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT));
        verifyNoInteractions(searchService);
    }

    @Test
    void rejectsReversedTimeRangeBeforeCallingSearchService() {
        var searchService = mock(ApplicationLogSearchService.class);
        var service = new AuthService(
                mock(AuthOperationsService.class), searchService, List.of(new ApplicationLogSearchTimeValidator()));
        var filter = new ApplicationLogSearchFilter(
                null,
                null,
                LocalDateTime.parse("2026-09-27T11:00:00"),
                LocalDateTime.parse("2026-09-27T10:00:00"),
                null);
        assertThrows(
                InvalidApplicationLogSearchException.class,
                () -> service.searchApplicationLogs(filter, 0, 50, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT));
        verifyNoInteractions(searchService);
    }
}
