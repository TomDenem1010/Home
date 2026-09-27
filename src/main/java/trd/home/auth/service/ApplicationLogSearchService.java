package trd.home.auth.service;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.dto.ApplicationLogSearchResult;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

@Service
@RequiredArgsConstructor
public class ApplicationLogSearchService {
    private final ApplicationLogRepository repository;

    @Transactional(readOnly = true)
    public Page<ApplicationLogSearchResult> search(ApplicationLogSearchFilter filter, int page, int size) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(new Sort.Order(Sort.Direction.DESC, "createdAt"), new Sort.Order(Sort.Direction.DESC, "id")));
        return repository
                .findAll(matching(filter), pageable)
                .map(log -> new ApplicationLogSearchResult(
                        log.getMethod(),
                        log.getInput(),
                        log.getOutput(),
                        log.getError(),
                        log.getCreatedAt(),
                        log.getCreatedBy()));
    }

    private static Specification<ApplicationLog> matching(ApplicationLogSearchFilter filter) {
        return (root, query, builder) -> {
            var criteria = (HibernateCriteriaBuilder) builder;
            var predicates = new ArrayList<Predicate>();
            if (filter.text() != null && !filter.text().isBlank()) {
                for (String word :
                        filter.text().strip().toLowerCase(Locale.ROOT).split("\\s+")) {
                    String pattern = containsPattern(word);
                    predicates.add(builder.or(
                            criteria.ilike(root.get("method"), pattern, '\\'),
                            criteria.ilike(root.get("input"), pattern, '\\'),
                            criteria.ilike(root.get("output"), pattern, '\\')));
                }
            }
            if (filter.createdBy() != null && !filter.createdBy().isBlank()) {
                predicates.add(
                        builder.equal(root.get("createdBy"), filter.createdBy().strip()));
            }
            if (filter.timeStart() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.<Instant>get("createdAt"), filter.timeStart().toInstant(ZoneOffset.UTC)));
            }
            if (filter.timeEnd() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.<Instant>get("createdAt"), filter.timeEnd().toInstant(ZoneOffset.UTC)));
            }
            if (filter.error() != null) {
                predicates.add(
                        filter.error() ? builder.isNotNull(root.get("error")) : builder.isNull(root.get("error")));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String containsPattern(String value) {
        return "%" + value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
