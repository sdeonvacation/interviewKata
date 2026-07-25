# Plan: InterviewKata — Interview Fundamentals Platform

## Overview

Standalone web platform for strengthening core technical interview fundamentals. Targets the gap between "getting interviews in Germany" and "passing them" through daily deliberate practice, AI-evaluated answers, and spaced repetition across Java, Spring Boot, system design, DSA, databases, and architecture patterns.

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Java 21 + Spring Boot 3.3 | Latest stable |
| Build | Gradle (Kotlin DSL) | 8.x |
| Database | PostgreSQL 16 | Via Docker |
| Migrations | Liquibase | Spring-managed |
| AI | Spring AI + Anthropic/OpenAI | Same provider config as JobHunter |
| Code Execution | JShell (JDK built-in) | Sandboxed |
| Frontend | React 18 + Vite + Tailwind CSS | TypeScript |
| Spaced Repetition | SM-2 algorithm | Custom implementation |
| Testing | JUnit 5, Vitest | Standard |

## Testing Strategy

- Unit: SM-2 scheduling logic, content parsing, AI prompt formatting, challenge evaluation
- Integration: JShell sandbox execution, AI provider calls, full review session flow
- Frontend: Component tests for review UI, quiz flow, code editor interaction
- Done when: Each phase has passing tests for core logic before proceeding to next

## Phases

### Phase 1: Foundation (Project Skeleton + Core Domain)

- Step 1: Initialize Spring Boot project with Gradle, PostgreSQL, Liquibase, Spring AI
- Step 2: Define core domain models: Topic (hierarchical), Card, Challenge, Exercise, UserProgress
- Step 3: Create Liquibase migrations for all core tables
- Step 4: Build Topic tree CRUD API + seed topic hierarchy for all 6 focus areas
- Step 5: Scaffold React frontend with routing, Tailwind, dark theme
- Step 6: Simple single-user auth (no registration - personal platform)
- Step 7: Topic browser UI showing the knowledge tree

### Phase 2: Flashcards + Spaced Repetition

- Step 1: Implement SM-2 algorithm (interval, ease factor, repetition count)
- Step 2: Card model with front/back, code snippets, explanation, topic linkage
- Step 3: YAML-based card seed format + loader for curated content
- Step 4: Review session API (get due cards, submit grade, compute next review)
- Step 5: AI explanation generator (expand card back with deeper explanation on demand)
- Step 6: Review session UI (flip card, self-grade 1-5, see schedule)
- Step 7: Daily review queue widget ("12 cards due today")
- Step 8: Seed ~100 Java core cards, ~60 Spring Boot cards, ~50 DB cards

### Phase 3: Topic Guides + Quizzes

- Step 1: Guide content model (topic-linked, markdown sections, embedded code examples)
- Step 2: Quiz question model (MCQ, fill-blank, predict-output, explain-code)
- Step 3: Guide YAML format + seed essential guides per topic
- Step 4: AI quiz generator (given topic + guide content, generate N questions)
- Step 5: Quiz session API (start, answer, evaluate, score)
- Step 6: Guide reader UI with inline quiz checkpoints
- Step 7: Seed ~30 guides across focus areas

### Phase 4: Coding Dojo

- Step 1: Challenge model (problem statement, difficulty, test cases, hints, time limit)
- Step 2: JShell sandbox implementation (SecurityManager, timeout, memory cap)
- Step 3: Test runner (execute user code against test cases, capture output)
- Step 4: AI code reviewer (analyze submission, provide feedback on style/complexity)
- Step 5: Challenge API (list, submit, run, get hints)
- Step 6: Code editor UI (Monaco editor, run button, test results, AI feedback panel)
- Step 7: Seed ~40 DSA challenges, ~20 Java challenges, ~15 SQL challenges

### Phase 5: System Design + Mock Interviews

- Step 1: Design exercise model (prompt, constraints, evaluation rubric, reference approach)
- Step 2: AI rubric evaluator (score written design answer against criteria)
- Step 3: Mock interview engine (conversation state machine, topic-aware question selection)
- Step 4: Multi-turn AI interview (ask, follow-up based on answer quality, probe weak points)
- Step 5: Session transcript + scoring API
- Step 6: Design exercise UI (prompt, text editor, submit, see evaluation)
- Step 7: Mock interview UI (chat interface, timer, final scorecard)
- Step 8: Seed ~15 system design exercises, ~10 architecture exercises

### Phase 6: Dashboard + Progress Tracking

- Step 1: Streak tracking (daily activity detection, consecutive days counter)
- Step 2: Topic completion calculation (cards mastered / total, challenges solved / total)
- Step 3: Weak-area detection (topics with accuracy < 60%)
- Step 4: Daily focus recommendation engine (due cards + weak topics + variety)
- Step 5: Dashboard UI (today's focus, streak counter, topic heatmap, recent activity)
- Step 6: Study session history log

## Content Seeding Strategy

| Area | Cards | Challenges | Guides | Design Exercises |
|------|-------|-----------|--------|-----------------|
| Java Core | ~100 | ~20 | ~8 | — |
| Spring Boot | ~60 | ~10 | ~6 | — |
| System Design | ~20 | — | ~5 | ~15 |
| DSA | ~30 | ~40 | ~5 | — |
| Database & SQL | ~50 | ~15 | ~4 | — |
| Architecture | ~30 | — | ~4 | ~10 |

## Risks/Edge Cases

- **JShell security**: Sandbox with SecurityManager + 5s timeout + 256MB heap cap. Deny file/network/reflection access.
- **AI cost explosion**: Cache generated explanations, rate-limit mock interviews (3/day), batch card generation.
- **Content quality**: Thumbs-up/down on AI-generated content, flag mechanism to mark bad cards.
- **Scope creep**: Each phase independently useful. Phase 2 alone = daily flashcard practice. Ship incrementally.
- **Motivation decay**: Streak mechanism, "interview in N days" countdown mode, daily push notification.
- **SM-2 cold start**: New cards start at 1-day interval. Graduating interval = 1 day. Learning steps: 1min, 10min.
