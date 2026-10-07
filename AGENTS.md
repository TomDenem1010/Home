# Home project development rules

This file defines the architectural and development rules for the entire Home project.

These rules are mandatory for every change unless the task explicitly requires changing the architecture itself.

The existing architecture must be preserved and extended consistently. Do not introduce alternative architectural patterns for isolated features.

---

# 1. General principles

The application is a modular Spring Boot application organized primarily by business domain.

Current top-level application areas include:

```text
trd.home
├── auth
├── common
├── frontend
├── media
└── tcg
```

Business functionality belongs to a business module.

Modules are organized by topic/domain, not by technical layer across the whole application.

Examples:

- authentication and authorization → `auth`
- media handling → `media`
- trading card functionality → `tcg`
- web/UI layer → `frontend`
- reusable infrastructure → `common`

When adding a new independent business area, prefer creating a new top-level module instead of placing unrelated functionality into an existing module.

Do not create abstractions in advance. Introduce shared abstractions only when multiple existing use cases demonstrate that they are needed.

Prefer simple and explicit code over generic frameworks or excessive indirection.

---

# 2. Module boundaries

Business modules are isolated from each other.

For example:

```text
auth    ─┐
media   ─┼──> common
tcg     ─┘
```

The following dependency is forbidden:

```text
tcg -> media
media -> auth
auth -> tcg
```

More generally:

> One business module must not directly depend on another business module.

This applies to:

- services
- repositories
- DAOs/entities
- DTOs
- constants
- validators
- exceptions
- configuration classes

If functionality is genuinely required by multiple modules, extract the reusable part into `common`.

Do not move domain-specific functionality into `common` merely to bypass module boundaries.

`common` must remain domain-independent.

---

# 3. Standard business module structure

Business modules should follow the existing package structure where applicable.

Typical structure:

```text
<module>
├── configuration
├── constant
├── dao
├── dto
├── exception
├── repository
├── scheduler
├── service
│   ├── <ModuleName>Service.java
│   └── <topic-specific subpackages>
├── specification
└── validator
```

Not every module needs every package.

Create a package only when the module actually has functionality belonging there.

Do not create empty architectural layers.

Examples of valid module-specific service packages include:

```text
service/application
service/deck
service/event
service/file
```

The package name should describe the responsibility of the contained services.

---

# 4. Module root service

Every business module exposes a primary service facade under its root `service` package.

Examples:

```text
auth/service/AuthService.java
media/service/MediaService.java
tcg/service/TcgService.java
```

The naming convention is:

```text
<ModuleName>Service
```

This service is the primary public application-facing entry point of the module.

External callers, especially frontend controllers, should interact with the module through this service.

Example:

```java
@Controller
@RequiredArgsConstructor
public class TcgFrontendController {

    private final TcgService tcgService;
}
```

The module service delegates work to more specialized services inside the same module.

Example conceptual flow:

```text
TcgFrontendController
        |
        v
   TcgService
     /    \
    v      v
Command   Query
Service   Service
            |
            v
     domain-specific services
```

The root service should remain relatively thin.

Its responsibilities are primarily:

- exposing module operations;
- defining the module's external API;
- delegating to specialized internal services;
- performing small orchestration when appropriate;
- applying cross-cutting annotations belonging to that operation.

Do not accumulate complex business logic in the root module service when the logic has a clear specialized responsibility.

---

# 5. Internal module services

Specialized functionality belongs to focused services inside the module.

Services should be separated by responsibility rather than simply by size.

Examples:

```text
tcg/service/application/TcgCommandService
tcg/service/application/TcgQueryService
tcg/service/deck/CardmarketDeckSaver
tcg/service/deck/CardmarketDeckClearer
tcg/service/event/RefreshDeckPricesService
```

A service should have a clear reason to exist and a clear responsibility.

Avoid generic classes such as:

```text
HelperService
CommonService
UtilsService
ManagerService
```

unless the name genuinely describes a well-defined concept.

Prefer domain-specific names.

---

# 6. Frontend module

All MVC/web controllers belong to the `frontend` module.

Business modules must not contain frontend controllers.

Typical structure:

```text
frontend
├── auth
├── event
├── helper
├── media
└── tcg
```

Frontend packages may mirror business modules for organization, but they remain part of the `frontend` module.

For example:

```text
frontend/tcg/TcgFrontendController
```

calls:

```text
tcg/service/TcgService
```

A frontend controller must not call:

- another module's internal service;
- repositories;
- JPA entities for persistence operations;
- module-internal implementation classes.

The normal dependency flow is:

```text
HTTP request
    |
    v
Frontend Controller
    |
    v
<ModuleName>Service
    |
    v
module internal services
    |
    v
repositories
```

Controllers are responsible for:

- HTTP endpoint mapping;
- receiving and validating request-level parameters;
- invoking the appropriate module service;
- assembling the frontend model;
- selecting views and redirects;
- HTTP-specific behavior.

Controllers are not responsible for:

- persistence logic;
- repository access;
- domain algorithms;
- long-running processing;
- transaction orchestration;
- business rules.

Keep controllers thin.

---

# 7. Common module

`common` contains infrastructure and functionality that is genuinely reusable across multiple modules.

Examples of appropriate common responsibilities include:

```text
common/browser
common/configuration
common/constant
common/dao
common/dto
common/event
common/exception
common/file
common/logging
common/playwright
common/repository
common/validator
```

The `common` module may be used by all modules.

`common` must not depend on business modules.

Forbidden:

```text
common -> tcg
common -> media
common -> auth
```

Allowed:

```text
tcg   -> common
media -> common
auth  -> common
frontend -> common
```

Do not place code into `common` merely because two classes need to communicate.

A class belongs in `common` only if its concept itself is independent of the business modules using it.

For example, a generic event-processing mechanism may belong in `common`, while a TCG deck refresh event processor belongs in `tcg`.

---

# 8. Persistence and repositories

Persistence uses Spring Data JPA.

Repositories should use:

- standard `JpaRepository` functionality;
- Spring Data derived query methods;
- JPA Specifications where dynamic filtering is required;
- normal JPA entity relationships.

## `@Query` is forbidden

Do not use:

```java
@Query
```

This includes:

```java
@Query("...")
```

and:

```java
@Query(value = "...", nativeQuery = true)
```

Do not introduce JPQL or native SQL queries through repository annotations.

Prefer derived repository methods such as:

```java
findById(...)
findAllByStatus(...)
findAllByDeckVersionIdIn(...)
existsByName(...)
deleteById(...)
```

For complex and dynamic search conditions, use Spring Data JPA Specifications.

Example:

```java
Specification<Entity>
```

Do not bypass this rule because a query would be shorter to write with `@Query`.

If a repository operation becomes complicated, first reconsider:

- entity relationships;
- repository method naming;
- Specifications;
- decomposition of the operation.

Database-specific SQL belongs only in database/schema management where SQL is inherently required, not in application repository methods.

---

# 9. JPA entities

Entities belong to the module that owns the data.

Typical location:

```text
<module>/dao
```

Do not share business entities between modules.

Entity relationships should represent the actual domain cardinality.

Use database constraints where an invariant must also be guaranteed at persistence level.

Examples include:

- unique constraints;
- foreign keys;
- non-null columns.

Do not rely solely on Java-side validation for invariants that the database can and should enforce.

Be deliberate with:

- cascade settings;
- orphan removal;
- lazy/eager loading;
- bidirectional relationships.

Do not introduce `EAGER` loading merely to fix a lazy-loading problem.

---

# 10. Transactions

Transactions belong in the service layer.

Use:

```java
@Transactional
```

around operations that must execute atomically.

Do not place transaction boundaries in frontend controllers.

Keep transaction scope as narrow as practical.

Operations that modify multiple related entities and must either fully succeed or fully fail should normally execute within one transaction.

---

# 11. DTOs

DTOs belong to the module that owns the represented business concept.

Use DTOs for data crossing meaningful layer/API boundaries when exposing entities directly would couple layers unnecessarily.

Prefer immutable DTOs where practical, including Java records for simple data carriers.

Do not create DTOs merely to duplicate an entity field-for-field unless there is a real boundary or representation requirement.

Frontend-specific view representation should not leak into persistent entities.

---

# 12. Validation

Validation should happen at the appropriate layer.

Request/form-level validation may happen at the frontend boundary.

Domain-specific validation belongs inside the owning module.

Reusable generic validators may belong in `common`.

Do not rely exclusively on frontend validation for important rules.

All security-sensitive and business-critical validation must be enforced server-side.

---

# 13. Authentication and authorization

The application may contain multiple users with different permissions.

Although the application is normally used by one person at a time, user authorization must remain correct for every configured user.

Never assume that all users have identical permissions.

Authorization must be enforced on the server.

Do not rely on:

- hidden frontend controls;
- unavailable menu entries;
- JavaScript restrictions;
- a single-browser usage pattern

as security mechanisms.

Frontend visibility may reflect permissions, but backend authorization remains authoritative.

Do not store user-specific state in application-wide mutable singleton fields.

Any state whose value depends on the logged-in user must be associated with the actual authenticated user/session or passed explicitly.

---

# 14. Runtime assumptions

The expected normal usage model is:

- one active human user at a time;
- one browser;
- normally one browser tab;
- multiple configured application users may exist;
- users may have different roles and permissions.

Do not over-engineer the system for large-scale concurrent traffic unless a task explicitly requires it.

However, the single-user usage model must not be used to justify incorrect shared mutable state, broken authorization, unsafe persistence, or fundamentally non-thread-safe code.

Spring singleton services should remain stateless unless there is a specific and safe reason otherwise.

Database state should remain the authoritative source for persistent application state.

---

# 15. Events and background processing

Long-running or asynchronous operations should follow the project's existing event-processing architecture where applicable.

Typical flow:

```text
Frontend
   |
   v
Module Service
   |
   v
Command Service
   |
   v
ApplicationEventQueue
   |
   v
Scheduler
   |
   v
Event-specific Service
   |
   v
ApplicationEventProcessor
```

Reuse the existing event infrastructure instead of inventing an independent asynchronous mechanism for each feature.

Event types shared by the infrastructure belong in the appropriate common event/constant area.

Business-specific event processing belongs inside the owning business module.

Schedulers should primarily:

- poll/claim the appropriate event;
- delegate processing.

Do not place substantial business logic directly into scheduler classes.

---

# 16. Configuration

Application configuration should use Spring configuration and environment-driven properties.

Do not hardcode environment-specific values such as:

- database URLs;
- credentials;
- hostnames;
- local filesystem paths;
- secrets.

Use properties/environment variables.

Provide sensible defaults only where a default is safe and environment-independent.

Secrets must never be committed to the repository.

---

# 17. Docker compatibility

The final application runs in Docker.

Every change must therefore be compatible with containerized execution.

Do not assume:

- a developer's local absolute filesystem path;
- Windows-specific paths;
- files outside the container;
- interactive terminal access;
- IDE-provided environment variables;
- localhost referring to another container or host service.

Use configuration/environment variables for external resources.

Prefer classpath resources for immutable application resources packaged with the application.

When filesystem persistence is required, ensure the location can be configured and mounted as a Docker volume.

Code must be portable across operating systems.

Use Java APIs such as `Path` and `Files` instead of manually concatenating filesystem separators.

When introducing dependencies involving browsers, files, networking or native libraries, consider whether they are available and functional inside the Docker image.

A feature is not complete if it works locally but cannot run in the project's Docker environment.

---

# 18. Frontend conventions

The frontend is server-rendered and should follow the existing Thymeleaf and CSS approach.

New pages should integrate with:

- the existing page renderer;
- navigation/menu structure;
- existing templates;
- existing CSS patterns.

Avoid introducing a second frontend framework for isolated functionality.

Do not add frontend dependencies without a substantial reason.

Keep JavaScript small and focused where server-side rendering is sufficient.

URLs and controller structure should be consistent with the owning business module.

Example:

```text
/tcg/...
/media/...
```

The frontend should call module APIs through their root module services rather than recreating business logic.

---

# 19. Error handling

Use module-specific exceptions for module-specific failures.

Use common exceptions only for genuinely cross-module concepts.

Do not silently swallow exceptions.

When wrapping exceptions, preserve the original cause where appropriate.

Error messages should provide enough context to identify the failed operation without exposing secrets.

Expected business errors should be distinguished from unexpected technical failures where useful.

---

# 20. Logging

Use the project's existing logging infrastructure and conventions.

Prefer structured, meaningful application-level logging over scattered debug output.

Never use:

```java
System.out.println(...)
System.err.println(...)
```

for application logging.

Do not log:

- passwords;
- secrets;
- authentication credentials;
- sensitive session values.

Avoid excessive logging inside tight loops.

Use existing method-call logging mechanisms when they fit the surrounding code.

---

# 21. Caching

Caching may be used for data that is:

- expensive enough to justify caching;
- effectively immutable;
- stable for the relevant application lifetime;
- safe to share between users.

Be especially careful when caching user-dependent data.

Never cache user-specific results globally unless the cache key explicitly includes all state that affects the result.

Prefer existing Spring Cache infrastructure instead of implementing custom global maps.

---

# 22. Code style

Follow the existing project style.

Java identifiers and source-code terminology must remain in English.

Prefer:

- descriptive class names;
- descriptive method names;
- small focused methods;
- constructor injection;
- immutable local values where practical;
- clear domain terminology.

Avoid unnecessary comments that merely repeat the code.

Comments should explain non-obvious intent, constraints or architectural decisions.

Avoid unrelated refactoring while implementing a focused task.

Do not rename or reorganize unrelated code unless required by the task.

---

# 23. Class organization

Follow a consistent order inside Java classes.

Prefer:

1. static constants;
2. injected/member fields;
3. constructors if explicitly required;
4. public methods;
5. package-private/protected methods where appropriate;
6. private methods;
7. private helper types.

Do not scatter private helper methods between public API methods without a reason.

---

# 24. Lombok

Use Lombok consistently with the existing project.

Prefer existing patterns such as:

```java
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
```

Do not introduce Lombok annotations merely to reduce a trivial amount of code if they obscure behavior.

Do not use `@Data` indiscriminately on JPA entities.

---

# 25. Null handling

Follow the existing nullability patterns.

Prefer APIs that clearly express whether a result may be absent.

Use `Optional` where it improves repository/service semantics, especially for lookup results.

Do not suppress nullability or type-safety warnings merely to make compilation warnings disappear.

Do not introduce unchecked casts unless unavoidable and justified.

---

# 26. Tests

Only unit tests are allowed.

Do not add:

- Spring integration tests;
- JPA integration tests;
- `@SpringBootTest` tests;
- Testcontainers-based tests;
- tests requiring a real database;
- tests requiring Docker;
- end-to-end tests;
- full application-context tests.

Tests must run in isolation without starting the Spring application context or external infrastructure.

Use JUnit and Mockito for unit testing.

Dependencies should be mocked or replaced with simple test doubles where appropriate.

Production code must be structured so that business behavior can be tested through normal unit tests using constructor-injected dependencies.

New behavior must normally include unit tests.

Bug fixes should include a regression unit test whenever practical.

Maintain high unit-test coverage, with particular attention to:

- business rules;
- calculations and transformations;
- service routing and orchestration;
- validators;
- event processing;
- exception and failure paths;
- authorization-related logic;
- controller behavior;
- domain entity behavior.

Tests should verify observable behavior rather than implementation details.

Prefer testing a class directly:

```java
var repository = mock(SomeRepository.class);
var dependency = mock(SomeDependency.class);

var service = new SomeService(repository, dependency);
```

Do not start Spring merely to perform dependency injection.

Controller tests must also remain unit-level. Instantiate the controller directly and mock the module service or other collaborators.

Where rendered HTML behavior must be tested, use the smallest isolated setup possible without loading the Spring application context. Do not turn such tests into application integration tests.

Do not test Spring Data JPA, Hibernate, Spring MVC, or other framework functionality itself. Assume framework behavior is covered by the framework's own tests.

Repository method definitions should therefore not receive database integration tests solely to verify that Spring Data interprets a derived query correctly.

Focus project tests on logic owned by this codebase.

Test edge cases in addition to the happy path, including where relevant:

- empty input;
- missing data;
- invalid values;
- duplicate data;
- boundary values;
- ordering;
- quantity changes;
- authorization differences;
- dependency failures;
- partial-operation failures.

Test names should clearly describe the expected behavior.

---

# 27. Testability

Production code should remain naturally testable.

Avoid:

- static mutable dependencies;
- hidden global state;
- direct construction of major dependencies inside services;
- business logic coupled to HTTP infrastructure;
- unnecessarily private monolithic algorithms.

Prefer constructor-injected collaborators.

Do not distort production architecture solely to satisfy a mocking framework.

---

# 28. Spotless

Spotless is mandatory.

After modifying Java code, run:

```bash
mvn spotless:apply
```

Generated or modified Java code must conform to Spotless formatting.

Do not manually fight the formatter.

If formatting changes unrelated lines because of the configured formatter, those formatting changes are acceptable, but avoid unrelated semantic modifications.

---

# 29. Required verification

After completing a code change, perform the relevant verification.

At minimum for Java changes:

```bash
mvn spotless:apply
mvn test
git diff --check
```

For focused changes, targeted tests may be run during development, but the complete relevant test suite should pass before considering the work complete when practical.

When Docker-related behavior changes, also validate the relevant Docker build/runtime configuration.

When configuration or dependency changes are made, verify that the application still builds from a clean environment.

If a required check cannot be executed, explicitly report that fact.

Never claim a test or build succeeded unless it was actually executed successfully.

---

# 30. Maven dependencies

Do not add a dependency when existing JDK or project functionality can reasonably solve the problem.

Before adding a dependency:

1. check whether the functionality already exists in the project;
2. prefer Spring/JDK functionality already available;
3. consider Docker impact;
4. consider maintenance and security implications.

Keep dependency scope as narrow as possible.

Do not introduce an additional framework that duplicates an existing project capability.

---

# 31. Database changes

Database changes must remain compatible with the project's existing database and migration/setup strategy.

When changing the data model:

- update database schema definitions/migrations;
- update JPA mappings;
- preserve database constraints;
- consider existing data;
- test the affected business behavior.

The database and JPA model must describe the same invariants.

Do not make an entity relationship stricter in Java while leaving contradictory database constraints behind, or vice versa.

---

# 32. Performance

Optimize based on the application's actual usage model.

This is a small application with one active human user in normal operation.

Readability and correctness are generally more important than micro-optimizations.

However, avoid obvious pathological behavior such as:

- unbounded repeated database queries;
- unnecessary N+1 access patterns in large loops;
- loading arbitrarily large datasets where paging already exists;
- repeatedly performing expensive browser/network operations;
- unnecessary repeated parsing of identical external data.

Do not add distributed caches, messaging systems or complex concurrency infrastructure without a concrete requirement.

---

# 33. Security

Treat authorization and credential handling as correctness requirements.

Never:

- commit secrets;
- expose passwords;
- trust authorization decisions made only by the frontend;
- construct filesystem or external-resource access unsafely from unchecked user input;
- weaken existing security configuration merely to make a feature easier to implement.

Different users may have different permissions even though only one user normally operates the application at a time.

Every privileged operation must respect the authenticated user's authorization.

---

# 34. Architecture decision rules

When implementing a new feature, determine ownership first.

Use this decision order:

```text
Is it UI/HTTP-specific?
    -> frontend

Is it specific to an existing business domain?
    -> that business module

Is it a new independent business domain?
    -> new top-level business module

Is it genuinely domain-independent and reused by multiple modules?
    -> common
```

Within a business module:

```text
External module API
    -> <ModuleName>Service

Application orchestration
    -> service/application or another focused service package

Domain-specific operation
    -> focused service

Persistence
    -> repository

Persistent state
    -> dao

Transfer/result model
    -> dto

Dynamic JPA filtering
    -> specification

Validation
    -> validator

Scheduled event polling
    -> scheduler
```

Do not solve ownership problems with cross-module imports.

---

# 35. Dependency direction summary

The intended architecture is:

```text
                    ┌──────────────┐
                    │   frontend   │
                    └──────┬───────┘
                           │
             ┌─────────────┼─────────────┐
             v             v             v
          ┌──────┐      ┌───────┐     ┌─────┐
          │ auth │      │ media │     │ tcg │
          └──┬───┘      └───┬───┘     └──┬──┘
             │              │             │
             └──────────────┼─────────────┘
                            v
                       ┌────────┐
                       │ common │
                       └────────┘
```

Allowed:

```text
frontend -> business module root service
frontend -> common
business module -> common
business module internal layer -> same module internal layer
```

Forbidden:

```text
business module -> another business module
common -> business module
frontend -> business module repository
frontend -> business module internal service
frontend -> business module DAO for persistence logic
```

The root module service is the boundary that external callers should normally see.

---

# 36. Before modifying existing architecture

Do not introduce a new architectural pattern merely because it is common in other Spring applications.

This repository intentionally uses its existing structure.

Before changing architecture, verify whether the existing pattern can support the requirement.

Prefer consistency with the surrounding project over theoretical architectural purity.

Examples:

- use the existing module facade instead of introducing a new API layer;
- use the existing event queue instead of introducing another async framework;
- use JPA repository methods or Specifications instead of `@Query`;
- use the existing frontend rendering model instead of introducing a separate frontend stack;
- use `common` only for truly shared infrastructure.

Large refactors should only be performed when required by the task or when the current structure demonstrably prevents a correct implementation.

---

# 37. Completion checklist

Before considering a task complete, verify:

- The code belongs to the correct module.
- No forbidden cross-module dependency was introduced.
- Frontend code calls the module through its root service.
- Business logic is not implemented in controllers.
- Repository access stays inside the owning module.
- No `@Query` annotation was introduced.
- JPA derived queries or Specifications are used instead.
- User permissions remain correctly enforced.
- No user-specific mutable singleton state was introduced.
- Docker compatibility was considered.
- Environment-specific values are configurable.
- Relevant tests were added or updated.
- Existing tests pass.
- `mvn spotless:apply` was run.
- `git diff --check` passes.
- The final diff contains no unrelated changes.

When reporting completion, summarize:

1. what changed;
2. which architectural areas were affected;
3. which tests/checks were executed;
4. whether any check could not be executed.
