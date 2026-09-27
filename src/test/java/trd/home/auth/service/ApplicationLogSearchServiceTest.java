package trd.home.auth.service;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaPredicate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import trd.home.auth.constant.ApplicationLogSort;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.dto.ApplicationLogSearchResult;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

class ApplicationLogSearchServiceTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("unchecked")
    void buildsAllCriteriaWithEscapedWordsUtcBoundsAndErrorSelection(boolean error) {
        var start = LocalDateTime.parse("2026-09-27T10:00:00");
        var end = start.plusHours(1);
        var filter = new ApplicationLogSearchFilter("  SAVE  10%_\\  ", " alice ", start, end, error);
        when(repository.findAll(ArgumentMatchers.<Specification<ApplicationLog>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.search(filter, 0, 10, Sort.Direction.DESC, ApplicationLogSort.CREATED_AT);
        var specification = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(specification.capture(), any(Pageable.class));
        assertNotNull(specification.getValue());
        Root<ApplicationLog> root = mock(Root.class);
        var builder = mock(HibernateCriteriaBuilder.class);
        Path<String> method = mock(Path.class);
        Path<String> input = mock(Path.class);
        Path<String> output = mock(Path.class);
        Path<String> creator = mock(Path.class);
        Path<String> failure = mock(Path.class);
        Path<Instant> time = mock(Path.class);
        when(root.<String>get("method")).thenReturn(method);
        when(root.<String>get("input")).thenReturn(input);
        when(root.<String>get("output")).thenReturn(output);
        when(root.<String>get("createdBy")).thenReturn(creator);
        when(root.<String>get("error")).thenReturn(failure);
        when(root.<Instant>get("createdAt")).thenReturn(time);
        var predicate = mock(JpaPredicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(predicate);
        assertSame(predicate, specification.getValue().toPredicate(root, mock(CriteriaQuery.class), builder));
        for (var field : List.of(method, input, output)) {
            verify(builder).ilike(field, "%save%", '\\');
            verify(builder).ilike(field, "%10\\%\\_\\\\%", '\\');
        }
        verify(builder).equal(creator, "alice");
        verify(builder).greaterThanOrEqualTo(time, start.toInstant(UTC));
        verify(builder).lessThanOrEqualTo(time, end.toInstant(UTC));
        if (error) {
            verify(builder).isNotNull(failure);
            verify(builder, never()).isNull(any());
        } else {
            verify(builder).isNull(failure);
            verify(builder, never()).isNotNull(any());
        }
        var predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicates.capture());
        assertEquals(6, predicates.getValue().length);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @SuppressWarnings("unchecked")
    void emptyFiltersProduceNoRestrictions(String empty) {
        when(repository.findAll(ArgumentMatchers.<Specification<ApplicationLog>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.search(
                new ApplicationLogSearchFilter(empty, empty, null, null, null),
                0,
                10,
                Sort.Direction.DESC,
                ApplicationLogSort.CREATED_AT);
        var specification = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(specification.capture(), any(Pageable.class));
        var builder = mock(HibernateCriteriaBuilder.class);
        var predicate = mock(JpaPredicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(predicate);
        assertSame(
                predicate, specification.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), builder));
        var predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicates.capture());
        assertEquals(0, predicates.getValue().length);
        verifyNoMoreInteractions(builder);
    }

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
        when(repository.findAll(ArgumentMatchers.<Specification<ApplicationLog>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(log), pageable, 25));
        var results =
                service.search(new ApplicationLogSearchFilter(null, null, null, null, null), 1, 10, direction, sort);
        assertEquals(25, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(10, results.getSize());
        assertEquals(
                new ApplicationLogSearchResult("saveCard", "input", "output", "failure", timestamp, "alice", 123L),
                results.getContent().getFirst());
        verify(repository).findAll(ArgumentMatchers.<Specification<ApplicationLog>>any(), eq(pageable));
    }
}
