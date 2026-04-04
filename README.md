# CryptoFolio

Portfolio project built to demonstrate `Java + Spring Boot + JUnit` skills using a Clean Architecture approach.

## Official Roadmap

The source of truth for scope and ticket order is:

- `documents/CryptoFolio_Roadmap.pdf`

Current focus is backend-only. Frontend work is intentionally out of scope until backend roadmap phases are complete.

## Backend Architecture

The backend follows a layered structure so business rules can grow without being coupled to Spring, HTTP, or persistence details.

Main rule:

- business concepts live in `domain`
- use cases live in `application`
- technical details live in `infrastructure`
- Spring wiring lives in `config`

Target backend structure:

```text
backend/src/main/java/com/cryptofolio/backend
  config/
  domain/
    model/
    valueobject/
    service/
    exception/
  application/
    usecase/
    port/
      in/
      out/
    dto/
  infrastructure/
    in/
      web/
        controller/
        request/
        response/
    out/
      persistence/
      client/
      config/
      mapper/
  shared/
```

## Package Responsibilities

### `domain`

Contains the business model and business rules.

- `domain/model`: entities and aggregates such as `User` or `Transaction`
- `domain/valueobject`: immutable concepts with validation such as `Money`, `Crypto`, or `TransactionType`
- `domain/service`: domain logic that does not naturally belong inside a single entity or value object
- `domain/exception`: domain-specific errors

Rules:

- must not import Spring
- must not import JPA
- must not know about controllers, requests, responses, or SQL

### `application`

Contains use cases that orchestrate domain behavior and interact through ports.

- `application/usecase`: use case implementations such as `RegisterUserUseCase`
- `application/port/in`: input contracts used by controllers or other entry points
- `application/port/out`: output contracts required by the use cases, such as repositories or token generators
- `application/dto`: input and output models used by the application layer

Rules:

- can depend on `domain`
- must not depend on HTTP or JPA details
- coordinates work, but does not contain framework-specific code

### `infrastructure`

Contains technical adapters that connect the application to the outside world.

- `infrastructure/in/web/controller`: REST controllers
- `infrastructure/in/web/request`: HTTP request models
- `infrastructure/in/web/response`: HTTP response models
- `infrastructure/out/persistence`: database adapters and persistence implementations
- `infrastructure/out/client`: adapters for external APIs or services
- `infrastructure/out/mapper`: mapping code between domain, persistence, and transport models
- `infrastructure/out/config`: infrastructure-specific technical configuration when needed

Rules:

- may use Spring, JPA, HTTP, external libraries, and clients
- implements ports defined in `application`
- must not contain business rules that belong in `domain` or `application`

### `config`

Contains Spring configuration and composition root concerns.

- bean wiring
- framework configuration
- cross-cutting technical setup

Rules:

- keeps configuration outside the core business layers
- should assemble the application without moving business logic into configuration classes

### `shared`

Contains truly shared utilities used across multiple layers.

Rules:

- use sparingly
- do not move unrelated logic here just because its final location is unclear

## Dependency Rules

Allowed dependency direction:

```text
infrastructure -> application -> domain
config --------> application / infrastructure
```

Important rules:

- `domain` is the most stable layer and must stay framework-agnostic
- `application` can use `domain`, but should depend on abstractions for external concerns
- `infrastructure` implements the ports declared by `application`
- `config` wires concrete implementations to the use cases

In practice:

- a controller calls a use case
- a use case calls a repository port
- an infrastructure adapter implements that repository port
- the domain stays unaware of HTTP, Spring, and persistence details

## Naming Conventions

Use names that reveal responsibility and layer.

- entities: `User`, `Transaction`
- value objects: `Money`, `Crypto`, `TransactionType`
- domain services: `PortfolioCalculator`
- use cases: `RegisterUserUseCase`, `LoginUserUseCase`
- input ports: `RegisterUserInputPort`
- output ports: `UserRepository`, `TokenGenerator`
- application DTOs: `RegisterUserCommand`, `RegisterUserResult`
- controllers: `AuthController`, `HealthController`
- HTTP requests: `RegisterUserRequest`, `LoginRequest`
- HTTP responses: `AuthResponse`, `HealthResponse`
- persistence adapters: `JpaUserRepositoryAdapter`
- persistence entities: `UserJpaEntity`
- mappers: `UserPersistenceMapper`, `AuthWebMapper`

Naming rules:

- avoid generic names such as `Manager`, `Helper`, or `ServiceImpl` unless the role is truly clear
- prefer names based on business intention instead of technical shortcuts
- if a class belongs to HTTP, persistence, or an external API, its name should make that explicit

## Examples

Examples of where classes should live:

- `User` -> `domain/model`
- `Money` -> `domain/valueobject`
- `Crypto` -> `domain/valueobject`
- `TransactionType` -> `domain/valueobject`
- `RegisterUserUseCase` -> `application/usecase`
- `UserRepository` -> `application/port/out`
- `RegisterUserCommand` -> `application/dto`
- `AuthController` -> `infrastructure/in/web/controller`
- `RegisterUserRequest` -> `infrastructure/in/web/request`
- `AuthResponse` -> `infrastructure/in/web/response`
- `JpaUserRepositoryAdapter` -> `infrastructure/out/persistence`
- `UserJpaEntity` -> `infrastructure/out/persistence`

Examples of what should not happen:

- putting validation of `Email` format in a controller
- putting registration business rules inside a JPA repository
- annotating `domain/model/User` with JPA annotations
- returning domain entities directly as HTTP responses by default

## Persistence Strategy

- Persistence stack: `Spring Data JPA`
- Migration strategy: `Flyway`
- Database engine: `PostgreSQL`
- Schema changes must be created as versioned SQL files under `backend/src/main/resources/db/migration`
- Hibernate must not create or update the schema automatically; Flyway is the source of truth

Current baseline migration:

- `V1__baseline.sql`

How schema creation works:

1. Start PostgreSQL locally
2. Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`
3. Start the backend
4. Flyway runs pending migrations automatically on startup
5. Future tables must be introduced through new files such as `V2__create_users_table.sql`

## Local Setup

Run from repository root:

```bash
docker compose up -d
```

Run backend tests:

```bash
cd backend
./mvnw test
```

## Testing Protocol

Testing style follows "just enough":

- cover all business behavior (valid, invalid, key edge cases)
- avoid brittle/noisy assertions tied to implementation details
- prefer `@ParameterizedTest` for repeated inputs
- keep tests minimal, clear, and maintainable
