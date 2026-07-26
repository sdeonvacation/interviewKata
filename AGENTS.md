# AGENTS.md

Guidance for AI coding agents working in the **InterviewKata** repository.

## Project Overview

InterviewKata is a full-stack software-engineering interview prep platform:
- **Backend:** Java 21 + Spring Boot 3.3 (`src/main/java/dev/interviewkata/`)
- **Frontend:** React 18 + TypeScript + Vite + Tailwind (`frontend/src/`)
- **DB:** PostgreSQL 16 via Docker (colima), Liquibase migrations
- **AI:** Spring AI → NVIDIA-hosted gpt-oss-120b (primary) + Google Gemini (fallback)

## Golden Rules

1. **Correctness over speed.** Verify against actual files/DB — never guess APIs, paths, or values.
2. **Surgical changes.** Touch only what the task needs; match existing conventions.
3. **Ask when unsure**, especially before destructive operations (dropping DB volumes, deleting rows).

## Build, Run, Test

```bash
make dev          # stop stale procs → start DB + backend(:5050) + frontend(:3002)
make stop         # kill by port (no orphans)
make test         # full suite

# Integration/E2E tests need Docker (Testcontainers):
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
./mvnw test
```

- Backend jar: `./mvnw -DskipTests package` → `target/interviewkata-0.0.1-SNAPSHOT.jar`
- Frontend typecheck: `cd frontend && tsc --noEmit`
- Run idempotent commands (tests, builds) **once**; tee output to a file and grep it rather than re-running.

## Environment Gotchas (IMPORTANT)

- **DB runs on colima `default` profile.** Use `DOCKER_HOST=unix://$HOME/.colima/default/docker.sock` for docker commands. DB port is `5436` (host) → `5432` (container).
- **Testcontainers + colima:** docker-java 3.3.6 negotiates Docker API v1.32 which modern colima rejects. `pom.xml`'s `maven-surefire-plugin` sets `api.version=1.43` (overridable with `-Dapi.version=<ver>`), and surefire forks inherit `DOCKER_HOST`/`TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE` from the shell env.
- **Lombok:** entities use `@Data`/`@Builder`. Editor/LSP will show false "method builder()/getX() undefined" errors — **ignore them; trust `./mvnw compile`.**
- **`make dev` never orphans:** `stop` kills by port (`lsof -ti tcp:5050`) to catch the child JVM that `mvn spring-boot:run` forks. Don't revert to PID-only kills.

## Backend Conventions

- Entities: JPA + Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`; child→parent refs are `@ManyToOne(LAZY)` with `@ToString.Exclude`/`@EqualsAndHashCode.Exclude`.
- DTOs are **Java records** in `dto/`; map via static methods in `DtoMapper`.
- Every schema change needs a **Liquibase migration** in `src/main/resources/db/changelog/` **AND** registration in `db.changelog-master.yaml` (Hibernate is `ddl-auto: validate` — an unregistered migration = startup failure).
- Auth: all `/api/**` require header `Authorization: Bearer dev-token` (`SimpleAuthFilter`).
- Challenge titles must be **globally unique** (dedup guard is global, not per-topic).

## AI Prompt Rules (`ai/PromptTemplates.java`)

- **System vs user split:** put instructions/rules in the **system** message and user/transcript content in the **user** message. Never merge them (prevents role confusion + injection).
- **Anti-hallucination:** the shared `ANTI_HALLUCINATION` clause is appended to knowledge-bearing prompts — keep it. Don't let the AI invent APIs, signatures, benchmarks, or citations; it must say "I'm not certain" when unsure.
- **Injection guards:** interview transcripts sanitize candidate input (strip `Interviewer:`/`Candidate:` labels and `[INTERVIEW_COMPLETE]`). Prompts instruct the model to treat transcript text as data, not commands.
- **Context isolation:** AI is **stateless** (no `ChatMemory` advisor). Each call passes only that session's transcript. Integration tests assert no cross-session leak — don't introduce shared conversation memory.
- **Java only** for code examples across all AI surfaces.
- Study topics are interpreted in the **interview-prep** context (e.g. "Behavioral" = STAR interview questions, not psychology).

## Frontend Conventions

- API client: `get`/`post`/`put`/`del` from `@/api/client` (auto-adds auth header).
- AI/markdown output renders via `MarkdownRenderer`; reusable AI Q&A via `AskAiPanel`.
- Route ordering: static segments before params (e.g. `/study/history` and `/study/session/:id` before `/study/:topicId`).
- Guard `useEffect` one-shot init synchronously (React StrictMode double-invokes effects in dev).

## Testing Conventions

- Unit: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`).
- Integration/E2E: `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate` + Testcontainers Postgres; **`@MockBean AiService`** (never hit the real LLM) — stub to echo captured context so tests can prove isolation.
- After changing a service/DTO signature, update all callers **and tests** (controllers, mocks, record constructors).

## Commit Etiquette

- Conventional prefixes: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- Inspect `git status`/`git diff` first; stage only intended files; never commit secrets (AI keys come from env).
