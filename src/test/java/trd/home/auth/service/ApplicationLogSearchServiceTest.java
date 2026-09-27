package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import trd.home.auth.constant.ApplicationLogSort;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.dto.ApplicationLogSearchResult;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

class ApplicationLogSearchServiceTest {
    private final ApplicationLogRepository repository = mock(ApplicationLogRepository.class);
    private final ApplicationLogSearchService service = new ApplicationLogSearchService(repository);

    @ParameterizedTest
    @CsvSource({"ASC, CREATED_AT", "DESC, CREATED_AT", "ASC, DURATION_MS", "DESC, DURATION_MS"})
    void passesPagingAndSortDirectionToRepositoryAndMapsAllResultFields(
            Sort.Direction direction, ApplicationLogSort sort) {
        var log = mock(ApplicationLog.class);
        var timestamp = Instant.parse("2026-09-27T11:00:00Z");
        when(log.getMethod()).thenReturn("saveCard");
        when(log.getInput()).thenReturn("input");
        when(log.getOutput()).thenReturn("output");
        when(log.getError()).thenReturn("failure");
        when(log.getCreatedAt()).thenReturn(timestamp);
        when(log.getCreatedBy()).thenReturn("alice");
        when(log.getDurationMs()).thenReturn(123L);
        var pageable = PageRequest.of(
                1, 10, Sort.by(new Sort.Order(direction, sort.getProperty()), new Sort.Order(direction, "id")));
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<ApplicationLog>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(log), pageable, 25));
        var results =
                service.search(new ApplicationLogSearchFilter(null, null, null, null, null), 1, 10, direction, sort);
        assertEquals(25, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(10, results.getSize());
        assertEquals(
                new ApplicationLogSearchResult("saveCard", "input", "output", "failure", timestamp, "alice", 123L),
                results.getContent().getFirst());
        verify(repository).findAll(org.mockito.ArgumentMatchers.<Specification<ApplicationLog>>any(), eq(pageable));
    }
}
