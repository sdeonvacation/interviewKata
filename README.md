# 🥋 InterviewKata

**Your dojo for software-engineering interview mastery.** Flashcards, real code execution, system-design drills, and an AI interviewer that actually pushes back — all in one dark-mode "Digital Dojo".

InterviewKata turns scattered interview prep into a disciplined daily practice: spaced-repetition flashcards, LeetCode-style coding challenges run against real test cases, Codemia-style system-design exercises, an AI mock-interviewer, behavioral STAR training, and a conversational **Study & Learn** tutor.

---

## ✨ Features

| Module | What it does |
| --- | --- |
| 🧠 **Knowledge Review** | SM-2 spaced-repetition flashcards (309 cards) across Java, Spring, DSA, DB, System Design, Architecture, Behavioral. Keyboard-driven (1-5 grade, Space to flip). |
| ⚡ **Coding Dojo** | 154 challenges (LeetCode Top 150 + extras) executed in a real **JShell sandbox** against per-test cases, with AI code review and post-solve reference solutions. |
| 🎯 **System Design** | 37 Codemia-style design exercises with AI evaluation against a rubric. |
| 🎤 **Mock Interviews** | A natural, multi-turn AI interviewer that assesses each answer, asks real follow-ups, and ends when satisfied — with a final scored evaluation. Full session history + delete. |
| 👥 **Behavioral** | 30 STAR-method cards across 6 categories + an AI behavioral interviewer that probes for individual contribution and measurable results. |
| 📚 **Study & Learn** | A ChatGPT-style tutor that *teaches* any topic interactively (guides, never just dumps answers). Multi-session per topic, persisted, tag-filterable history. |
| 📊 **Dashboard** | Daily training plan, streaks, due-card counts, and a systematic EASY→MEDIUM→HARD progression engine. |

Every AI surface renders **markdown**, uses **Java** for code examples, and is hardened against prompt-injection and cross-session context leaks.

---

## 🏗️ Tech Stack

- **Backend:** Java 21, Spring Boot 3.3, Spring AI, Spring Data JPA, Liquibase
- **Database:** PostgreSQL 16 (Docker)
- **Frontend:** React 18 + TypeScript, Vite, Tailwind CSS, lucide-react, react-markdown
- **AI:** NVIDIA-hosted openai/gpt-oss-120b (primary) with Google Gemini fallback
- **Testing:** JUnit 5, Mockito, Testcontainers (real Postgres integration/E2E)

---

## 🚀 Getting Started

### Prerequisites

- **Java 21**
- **Node 18+**
- **Docker** (via [colima](https://github.com/abiosoft/colima) or Docker Desktop) for PostgreSQL

### 1. Configure AI (env-driven)

All AI provider settings — endpoint, model, and key for both the primary and fallback — are read from the environment (`application.yaml` only holds sane defaults). The app runs without keys (AI features degrade gracefully), but for full functionality export:

```bash
# Primary provider (default: NVIDIA-hosted openai/gpt-oss-120b)
export INTERVIEWKATA_AI_PROVIDER="openai"        # primary client type: openai | anthropic
export INTERVIEWKATA_AI_API_KEY="<your-nvidia-api-key>"
export INTERVIEWKATA_AI_BASE_URL="https://integrate.api.nvidia.com"
export INTERVIEWKATA_AI_MODEL="openai/gpt-oss-120b"

# Fallback provider (default: Google Gemini)
export INTERVIEWKATA_AI_FALLBACK_API_KEY="<your-google-gemini-key>"
export INTERVIEWKATA_AI_FALLBACK_BASE_URL="https://generativelanguage.googleapis.com/v1beta/openai"
export INTERVIEWKATA_AI_FALLBACK_MODEL="gemini-2.0-flash"
```

Any provider with an OpenAI-compatible endpoint works — just point `*_BASE_URL` / `*_MODEL` / `*_API_KEY` at it. Only the API keys are strictly required; base-url and model fall back to the defaults above if unset.

### 2. Start everything with one command

```bash
make dev
```

This will:
1. **Stop** any previous run (kills stale processes by port — no orphans)
2. Start **PostgreSQL** (Docker, port `5436`)
3. Start the **backend** (Spring Boot, port `5050`) and wait until healthy
4. Start the **frontend** (Vite, port `3002`)

Then open **http://localhost:3002** 🎉

```
Backend:  http://localhost:5050
Frontend: http://localhost:3002
Auth:     Authorization: Bearer dev-token
```

### 3. Stop everything

```bash
make stop
```

---

## 🛠️ Make Targets

| Command | Description |
| --- | --- |
| `make dev` | Stop stale processes, then start DB + backend + frontend |
| `make stop` | Stop backend + frontend (kills by port, no orphans) |
| `make db` / `make db-stop` | Start / stop only the PostgreSQL container |
| `make db-backup` | Dump current DB content → `db/backup/interviewkata.sql` (committed snapshot) |
| `make db-restore` | Load the committed snapshot into your local DB |
| `make test` | Run the full test suite (unit + integration) |
| `make build` | Build the backend jar + frontend production bundle |
| `make clean` | Clean build artifacts |

---

## 🌱 Seeding & Sharing Content

The app **auto-seeds** all flashcards, coding challenges, and design exercises from the YAML files in `src/main/resources/seed/` on first startup — so a fresh `make dev` gives you the full content with zero extra steps.

A **ready-made database snapshot** is also committed at `db/backup/interviewkata.sql` (schema + all content + Liquibase state). To load it into your local DB — useful to skip seeding, share progress, or get identical data across machines:

```bash
make db            # start the PostgreSQL container
make db-restore    # load db/backup/interviewkata.sql into it
```

Or manually:

```bash
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock \
  docker exec -i interviewkata-db \
  psql -U interviewkata -d interviewkata < db/backup/interviewkata.sql
```

The snapshot is created with `--clean --if-exists`, so restoring is **safe on an empty or an existing** database (it drops and recreates objects first). To refresh the committed snapshot after adding content, run `make db-backup` and commit the updated file.

---

## 🧪 Testing

Unit tests run anywhere. Integration/E2E tests use **Testcontainers** (real Postgres) and require Docker:

```bash
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
./mvnw test
```

Coverage includes end-to-end flows, error paths, **AI context-leak isolation**, prompt-injection guards, and session persistence.

---

## 📁 Project Layout

```
interviewKata/
├── src/main/java/dev/interviewkata/
│   ├── ai/            # AiService + PromptTemplates (Gemini/NVIDIA)
│   ├── controller/    # REST endpoints
│   ├── service/       # Business logic (SM-2, interviews, study, review)
│   ├── model/         # JPA entities
│   ├── repository/    # Spring Data repositories
│   ├── dto/           # Records + DtoMapper
│   ├── sandbox/       # JShell code execution
│   └── seed/          # YAML content seeders
├── src/main/resources/
│   ├── db/changelog/  # Liquibase migrations
│   └── seed/          # Flashcards, challenges, exercises, solutions (YAML)
├── frontend/src/
│   ├── pages/         # Dashboard, Review, Coding, Design, Interviews, Study
│   ├── components/    # ChatBubble, MarkdownRenderer, AskAiPanel, …
│   └── hooks/         # useReviewSession, useInterviewSession, …
├── Makefile
└── docker-compose.yml
```

---

## 🔑 Configuration

Key settings in `src/main/resources/application.yaml`:

| Setting | Default |
| --- | --- |
| Server port | `5050` |
| DB URL | `jdbc:postgresql://localhost:5436/interviewkata` |
| Sandbox timeout | `5000 ms` |
| SM-2 graduating interval | `21 days` |
| AI provider (primary / fallback) | NVIDIA `openai/gpt-oss-120b` (OpenAI-compat) / Google Gemini |
| AI provider toggle | `INTERVIEWKATA_AI_PROVIDER=openai` (default) or `anthropic` |

---

## 📜 License

Personal project — use freely for your own interview prep. 🥋
