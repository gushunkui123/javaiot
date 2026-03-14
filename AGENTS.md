# Repository Guidelines

## Project Structure & Module Organization
- `agileboot-admin`: Spring Boot entry module and web controllers; this is the main runnable backend application.
- `agileboot-domain`: core business logic, domain models, commands, factories, and DB-facing services.
- `agileboot-infrastructure`: framework integration such as security, cache, MyBatis Plus, scheduling, filters, and runtime config.
- `agileboot-common`: shared constants, enums, exceptions, annotations, and utility classes.
- `agileboot-api`: reserved API/customize surface for external or app-facing endpoints.
- `sql/` stores database initialization scripts, `docs/` holds project docs, and `docker/` contains container-related assets.

## Build, Test, and Development Commands
- `.\mvnw.cmd clean install` builds all modules and packages the project.
- `.\mvnw.cmd test -DskipTests=false` runs the test suite from the root. The parent `pom.xml` defaults to `skipTests=true`, so set this flag explicitly.
- `.\mvnw.cmd -pl agileboot-domain test -DskipTests=false` runs tests for one module.
- `.\mvnw.cmd -pl agileboot-admin spring-boot:run -Dspring-boot.run.profiles=dev` starts the backend with the `dev` profile.
- Import the latest script in `sql/` before local startup, then update `agileboot-admin/src/main/resources/application-dev.yml`.

## Coding Style & Naming Conventions
- Target Java 8, UTF-8, and the repository Google style file: `GoogleStyle.xml`.
- Use 4-space indentation, lower-case package names, `PascalCase` for classes, and `camelCase` for fields and methods.
- Follow existing suffixes: `*Controller`, `*Model`, `*Factory`, `*Service`, `*Command`, `*Entity`, `*Test`.
- Keep business rules in `agileboot-domain`; keep transport/web concerns in `agileboot-admin`.

## Testing Guidelines
- Tests use JUnit 5, Spring Boot Test, and Mockito.
- Place tests under `src/test/java`; keep test resources in `src/test/resources`.
- Use `*Test.java` names mirroring the production type, for example `UserModelTest`.
- Add integration coverage when changing persistence, permissions, or profile-specific behavior. Existing DB integration tests live under `agileboot-domain/src/test/java/com/agileboot/integrationTest/db`.

## Commit & Pull Request Guidelines
- Git history is currently minimal (`init`), so prefer short imperative commit subjects with scope, for example `domain: validate menu parent rules`.
- Keep commits focused; separate refactors from behavior changes.
- PRs should include a clear summary, affected modules, config or SQL changes, linked issues, and test evidence. Include request/response examples when endpoints or auth behavior change.

## Security & Configuration Tips
- Do not commit real secrets in `application-*.yml`.
- Use PowerShell command examples for repository documentation and automation.
- When changing schema or seed data, update `sql/` and document any migration impact in the PR.
