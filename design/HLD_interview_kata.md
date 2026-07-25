# HLD: InterviewKata — Interview Fundamentals Practice Platform

## Tech Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Language | Java 21 (Temurin) | Backend logic, JShell execution engine |
| Framework | Spring Boot 3.3 | REST API, DI, scheduling, AI integration |
| Build | Gradle (Kotlin DSL) 8.x | Dependency management, multi-source set |
| Database | PostgreSQL 16 | Persistent storage for content + progress |
| Migrations | Liquibase | Schema versioning |
| AI | Spring AI (Anthropic/OpenAI) | Answer evaluation, content gen, mock interviews |
| Code Exec | JShell (JDK built-in) | Sandboxed Java code execution |
| Frontend | React 18 + Vite + Tailwind CSS | SPA with TypeScript |
| Testing | JUnit 5 + Vitest | Backend unit/integration + frontend component |
| Containerization | Docker Compose | PostgreSQL + (optional) app container |

## Components

| Component | Responsibility | Dependencies |
|-----------|---------------|--------------|
| TopicService | Hierarchical topic tree CRUD + seeding | TopicRepository |
| CardService | Flashcard CRUD, seed loading from YAML | CardRepository, TopicService |
| SM2Scheduler | Spaced repetition interval computation | None (pure algorithm) |
| ReviewSessionService | Orchestrate review: due cards, grading, scheduling | SM2Scheduler, CardService, UserProgressRepository |
| GuideService | Guide content CRUD, markdown rendering | GuideRepository, TopicService |
| QuizService | Quiz session lifecycle: start, answer, score | QuizQuestionRepository, AiService |
| ChallengeService | Code challenge CRUD, submission orchestration | ChallengeRepository, JShellSandbox, AiService |
| JShellSandbox | Sandboxed code execution with timeout/memory cap | JDK JShell API |
| TestRunner | Execute user code against predefined test cases | JShellSandbox |
| DesignExerciseService | System design exercise evaluation | DesignExerciseRepository, AiService |
| MockInterviewEngine | Conversation state machine, multi-turn AI interview | AiService, TopicService |
| AiService | Unified AI abstraction (evaluate, generate, converse) | Spring AI ChatClient |
| ContentSeeder | YAML parsing + DB insertion for all seed content | All content repositories |
| ProgressService | Streaks, completion %, weak-area detection | UserProgressRepository, all content repos |
| DashboardService | Daily focus recommendations, activity aggregation | ProgressService, ReviewSessionService |
| AuthFilter | Simple token/session guard (single-user) | Application config |

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React + Vite)                           │
│                                                                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────────┐  │
│  │Dashboard │ │ Review   │ │  Coding  │ │  Mock    │ │   Topic     │  │
│  │  Page    │ │ Session  │ │  Dojo    │ │Interview │ │  Browser    │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────┬──────┘  │
└───────┼─────────────┼────────────┼────────────┼──────────────┼──────────┘
        │             │            │            │              │
════════╪═════════════╪════════════╪════════════╪══════════════╪═══════════
        ▼             ▼            ▼            ▼              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         REST API LAYER                                   │
│                                                                         │
│  /api/topics     /api/cards     /api/challenges   /api/interviews       │
│  /api/reviews    /api/guides    /api/exercises    /api/dashboard         │
│  /api/quizzes    /api/progress                                          │
└───────┬────────────────────────────┬────────────────────────────────────┘
        │                            │
        ▼                            ▼
┌────────────────────────┐  ┌────────────────────────────────────────────┐
│    DOMAIN SERVICES     │  │          INFRASTRUCTURE                     │
│                        │  │                                            │
│  TopicService          │  │  ┌──────────────────┐                     │
│  CardService           │  │  │   AiService      │                     │
│  ReviewSessionService  │  │  │  (Spring AI)     │                     │
│  SM2Scheduler          │  │  │  - evaluate()    │                     │
│  GuideService          │  │  │  - generate()    │                     │
│  QuizService           │  │  │  - converse()    │                     │
│  ChallengeService      │  │  └────────┬─────────┘                     │
│  DesignExerciseService │  │           │                                │
│  MockInterviewEngine   │  │  ┌────────▼─────────┐  ┌────────────────┐ │
│  ProgressService       │  │  │ Anthropic/OpenAI │  │  JShell        │ │
│  DashboardService      │  │  │ (via Spring AI)  │  │  Sandbox       │ │
│  ContentSeeder         │  │  └──────────────────┘  │  - exec()      │ │
│                        │  │                        │  - timeout 5s   │ │
└───────┬────────────────┘  │                        │  - heap 256MB  │ │
        │                   │                        └────────────────┘ │
        │                   └────────────────────────────────────────────┘
        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      DATA LAYER (PostgreSQL 16)                          │
│                                                                         │
│  topic │ card │ card_review │ guide │ quiz_question │ quiz_session      │
│  challenge │ submission │ design_exercise │ mock_interview │ interview_turn│
│  user_progress │ daily_activity │ study_session                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Description**: Three-tier architecture with clear boundaries. Domain services encapsulate business logic and are independent of transport layer. Infrastructure layer isolates external concerns (AI providers, JShell process management). The AI layer uses Spring AI's ChatClient abstraction allowing provider swaps via config. JShell sandbox runs in a separate process with security constraints. Content seeding is a boot-time operation from YAML files. Single-user auth is a simple filter — no OAuth/OIDC complexity.

## Package Structure

```
dev.interviewkata/
├── InterviewKataApplication.java
├── config/
│   ├── SecurityConfig.java          # Simple auth filter registration
│   ├── AiConfig.java                # Spring AI ChatClient beans
│   ├── JShellConfig.java            # Sandbox configuration properties
│   └── ContentSeedConfig.java       # Seed-on-startup conditional bean
├── controller/
│   ├── TopicController.java         # GET /api/topics/**
│   ├── CardController.java          # GET/POST /api/cards/**
│   ├── ReviewController.java        # POST /api/reviews/**
│   ├── GuideController.java         # GET /api/guides/**
│   ├── QuizController.java          # POST /api/quizzes/**
│   ├── ChallengeController.java     # GET/POST /api/challenges/**
│   ├── ExerciseController.java      # GET/POST /api/exercises/**
│   ├── InterviewController.java     # POST /api/interviews/**
│   ├── DashboardController.java     # GET /api/dashboard/**
│   └── ProgressController.java      # GET /api/progress/**
├── dto/
│   ├── DtoMapper.java               # Static mapping methods
│   ├── TopicDto.java
│   ├── CardDto.java
│   ├── ReviewSessionDto.java
│   ├── ChallengeDto.java
│   ├── SubmissionResultDto.java
│   ├── InterviewTurnDto.java
│   └── DashboardDto.java
├── model/
│   ├── Topic.java
│   ├── Card.java
│   ├── CardReview.java
│   ├── Guide.java
│   ├── QuizQuestion.java
│   ├── QuizSession.java
│   ├── QuizAnswer.java
│   ├── Challenge.java
│   ├── Submission.java
│   ├── DesignExercise.java
│   ├── DesignSubmission.java
│   ├── MockInterview.java
│   ├── InterviewTurn.java
│   ├── UserProgress.java
│   ├── DailyActivity.java
│   ├── StudySession.java
│   └── enums/
│       ├── TopicArea.java           # JAVA_CORE, SPRING_BOOT, SYSTEM_DESIGN, DSA, DATABASE, ARCHITECTURE
│       ├── CardStatus.java          # NEW, LEARNING, REVIEW, GRADUATED
│       ├── Difficulty.java          # EASY, MEDIUM, HARD
│       ├── QuestionType.java        # MCQ, FILL_BLANK, PREDICT_OUTPUT, EXPLAIN_CODE
│       ├── ChallengeType.java       # DSA, JAVA, SQL
│       ├── SubmissionStatus.java    # RUNNING, PASSED, FAILED, TIMEOUT, ERROR
│       ├── InterviewState.java      # ASKING, WAITING, FOLLOW_UP, PROBING, COMPLETE
│       └── InterviewPhase.java      # INTRO, TECHNICAL, DEEP_DIVE, WRAP_UP
├── repository/
│   ├── TopicRepository.java
│   ├── CardRepository.java
│   ├── CardReviewRepository.java
│   ├── GuideRepository.java
│   ├── QuizQuestionRepository.java
│   ├── QuizSessionRepository.java
│   ├── ChallengeRepository.java
│   ├── SubmissionRepository.java
│   ├── DesignExerciseRepository.java
│   ├── MockInterviewRepository.java
│   ├── UserProgressRepository.java
│   └── DailyActivityRepository.java
├── service/
│   ├── TopicService.java
│   ├── CardService.java
│   ├── ReviewSessionService.java
│   ├── GuideService.java
│   ├── QuizService.java
│   ├── ChallengeService.java
│   ├── DesignExerciseService.java
│   ├── MockInterviewEngine.java
│   ├── ProgressService.java
│   └── DashboardService.java
├── ai/
│   ├── AiService.java               # Unified AI facade
│   ├── AnswerEvaluator.java         # Evaluate free-text answers
│   ├── CodeReviewer.java            # Review code submissions
│   ├── ContentGenerator.java        # Generate explanations, quiz questions
│   ├── InterviewConductor.java      # Multi-turn interview AI logic
│   └── PromptTemplates.java         # All prompt templates as constants
├── sandbox/
│   ├── JShellSandbox.java           # Core sandbox execution engine
│   ├── SandboxConfig.java           # Security policy, limits
│   ├── TestRunner.java              # Run test cases against user code
│   ├── ExecutionResult.java         # Stdout, stderr, pass/fail, timing
│   └── SecurityPolicy.java         # Custom security policy for JShell
├── seed/
│   ├── ContentSeeder.java           # Boot-time YAML → DB loader
│   ├── YamlCardParser.java          # Parse card YAML format
│   ├── YamlGuideParser.java         # Parse guide YAML format
│   └── YamlChallengeParser.java     # Parse challenge YAML format
├── scheduling/
│   └── SM2Scheduler.java            # Pure SM-2 algorithm implementation
└── auth/
    └── SimpleAuthFilter.java        # Token validation filter
```

## Interfaces

### SM2Scheduler

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| computeNext | grade: int (1-5), currentInterval: int, easeFactor: double, repetitions: int | SM2Result(nextInterval, newEaseFactor, newRepetitions) | Apply SM-2 formula: if grade >= 3 → advance interval; if < 3 → reset to learning | IllegalArgumentException if grade not 1-5 |
| getDueCards | userId: UUID, limit: int | List\<Card\> | Query cards where nextReviewDate <= now, ordered by overdue duration | None |
| isGraduated | card: Card | boolean | True if interval > graduating threshold (21 days) | None |

### AiService

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| evaluateAnswer | question: String, answer: String, rubric: String | EvaluationResult(score 0-10, feedback, strengths, weaknesses) | Send to AI with evaluation prompt, parse structured response | AiServiceException on provider failure |
| generateExplanation | topic: String, concept: String, depth: String | String (markdown) | Generate deeper explanation of a card/concept | AiServiceException |
| generateQuizQuestions | guideContent: String, count: int, type: QuestionType | List\<GeneratedQuestion\> | Generate N questions from guide content | AiServiceException |
| reviewCode | code: String, challenge: Challenge | CodeReviewResult(feedback, suggestions, complexity) | Analyze submission style/approach | AiServiceException |
| conductInterview | transcript: List\<InterviewTurn\>, topic: Topic, phase: InterviewPhase | InterviewResponse(question, followUpHint, phase) | Generate next interview question based on transcript context | AiServiceException |

### JShellSandbox

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| execute | code: String, timeoutMs: long | ExecutionResult(stdout, stderr, exitStatus, durationMs) | Run code in isolated JShell process with security restrictions | TimeoutException, SandboxSecurityException |
| executeWithTests | code: String, testCases: List\<TestCase\> | TestResult(passed: int, failed: int, details: List\<TestCaseResult\>) | Execute code then run each test case | TimeoutException, SandboxSecurityException |
| isHealthy | — | boolean | Check sandbox process pool health | None |

### ReviewSessionService

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| startSession | topicId: UUID (optional), limit: int | ReviewSession(cards, sessionId) | Fetch due cards (optionally filtered by topic), create session record | NoCardsException if queue empty |
| gradeCard | sessionId: UUID, cardId: UUID, grade: int | GradeResult(nextReviewDate, newInterval, cardsRemaining) | Apply SM-2, persist review, update progress | InvalidGradeException, SessionNotFoundException |
| getSessionSummary | sessionId: UUID | SessionSummary(total, correct, avgGrade, timeSpent) | Aggregate session statistics | SessionNotFoundException |

### MockInterviewEngine

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| startInterview | topicArea: TopicArea, difficulty: Difficulty | MockInterview(id, firstQuestion, timeLimit) | Create interview, generate opening question | RateLimitException (3/day) |
| submitAnswer | interviewId: UUID, answer: String | InterviewTurn(evaluation, nextQuestion, phase, isComplete) | Evaluate answer, decide follow-up/probe/advance | InterviewNotFoundException, InterviewCompleteException |
| endInterview | interviewId: UUID | InterviewScorecard(overallScore, categoryScores, transcript, recommendations) | Summarize performance, generate final scorecard | InterviewNotFoundException |

### ChallengeService

| Method | Input | Output | Behavior | Errors |
|--------|-------|--------|----------|--------|
| listChallenges | type: ChallengeType, difficulty: Difficulty, page: int | Page\<ChallengeDto\> | Paginated challenge list with solve status | None |
| getChallenge | challengeId: UUID | ChallengeDetailDto(problem, examples, constraints, hints) | Full challenge details | NotFoundException |
| submitSolution | challengeId: UUID, code: String, language: String | SubmissionResult(status, testResults, aiReview, executionTime) | Execute in sandbox, run tests, optionally AI review | SandboxException |
| runCode | code: String | ExecutionResult(stdout, stderr, duration) | Quick run without test evaluation (playground mode) | SandboxException |

## Data Flow

### Flow 1: Flashcard Review Session

| Step | Component | Action | Next |
|------|-----------|--------|------|
| 1 | ReviewController | POST /api/reviews/start → validate request | ReviewSessionService |
| 2 | ReviewSessionService | Query due cards via SM2Scheduler.getDueCards() | CardRepository |
| 3 | CardRepository | SELECT cards WHERE next_review <= NOW ORDER BY overdue DESC | Return to service |
| 4 | ReviewSessionService | Create StudySession record, return card batch | Controller → Client |
| 5 | Client | User views card, self-grades (1-5) | ReviewController |
| 6 | ReviewController | POST /api/reviews/{sessionId}/grade | ReviewSessionService |
| 7 | ReviewSessionService | SM2Scheduler.computeNext(grade, interval, ease, reps) | Persist |
| 8 | CardReviewRepository | INSERT card_review, UPDATE card (next_review, ease_factor) | Return |
| 9 | ReviewSessionService | Update DailyActivity + UserProgress | Response to client |

**Error Flows**: Invalid grade (1-5 validation at controller). No due cards → return empty session with "all caught up" message. DB failure → 500 with retry suggestion.

### Flow 2: Code Challenge Submission

| Step | Component | Action | Next |
|------|-----------|--------|------|
| 1 | ChallengeController | POST /api/challenges/{id}/submit with code body | ChallengeService |
| 2 | ChallengeService | Load challenge + test cases from DB | JShellSandbox |
| 3 | JShellSandbox | Fork isolated process, apply security policy | TestRunner |
| 4 | TestRunner | Compile user code, execute each test case sequentially | Collect results |
| 5 | JShellSandbox | Enforce 5s timeout, 256MB heap; kill if exceeded | Return ExecutionResult |
| 6 | ChallengeService | If all tests pass + AI review enabled → AiService.reviewCode() | AiService |
| 7 | AiService | Send code + challenge context to AI, parse review | Return CodeReviewResult |
| 8 | ChallengeService | Persist Submission record (status, test results, AI feedback) | Controller |
| 9 | ChallengeController | Return SubmissionResultDto (pass/fail, feedback, execution time) | Client |

**Error Flows**: Timeout → return TIMEOUT status with partial output. Security violation (file/network access) → SandboxSecurityException → SECURITY_ERROR status. AI unavailable → submission still processed, AI review field null.

### Flow 3: Mock Interview Turn

| Step | Component | Action | Next |
|------|-----------|--------|------|
| 1 | InterviewController | POST /api/interviews/{id}/answer | MockInterviewEngine |
| 2 | MockInterviewEngine | Load interview state + full transcript from DB | AiService |
| 3 | MockInterviewEngine | Determine phase: has user answered well? → advance. Poorly? → probe deeper | InterviewConductor |
| 4 | AiService.conductInterview | Build prompt with transcript context, generate next question | Return response |
| 5 | MockInterviewEngine | Persist InterviewTurn (user answer + AI evaluation + next question) | State update |
| 6 | MockInterviewEngine | Update state machine: check if phase complete, advance to next | Return turn |
| 7 | InterviewController | Return InterviewTurnDto (evaluation, next question, current phase) | Client |

**Error Flows**: Rate limit (3 interviews/day) → 429 with reset time. AI provider failure → retry once, then 503 with "interview paused" state (resumable). Interview already complete → 409.

## Data Model

### Core Content Tables

| Entity | Fields | Relationships | Constraints |
|--------|--------|---------------|-------------|
| topic | id: UUID, name: VARCHAR(200), area: ENUM, parent_id: UUID, description: TEXT, sort_order: INT, created_at: TIMESTAMP | Self-referencing parent (topic tree), cards, guides, challenges | UNIQUE(name, parent_id), NOT NULL(name, area) |
| card | id: UUID, topic_id: UUID, front: TEXT, back: TEXT, code_snippet: TEXT, explanation: TEXT, difficulty: ENUM, tags: VARCHAR[], status: ENUM, ease_factor: DOUBLE(default 2.5), interval_days: INT(default 0), repetitions: INT(default 0), next_review: TIMESTAMP, created_at: TIMESTAMP | belongs_to topic | NOT NULL(front, back, topic_id) |
| guide | id: UUID, topic_id: UUID, title: VARCHAR(300), content_markdown: TEXT, sort_order: INT, estimated_minutes: INT, created_at: TIMESTAMP | belongs_to topic, has_many quiz_questions | NOT NULL(title, content_markdown, topic_id) |
| quiz_question | id: UUID, guide_id: UUID, topic_id: UUID, question_type: ENUM, question_text: TEXT, options: JSONB, correct_answer: TEXT, explanation: TEXT, difficulty: ENUM, ai_generated: BOOLEAN | belongs_to guide, belongs_to topic | NOT NULL(question_text, correct_answer) |
| challenge | id: UUID, topic_id: UUID, title: VARCHAR(300), problem_statement: TEXT, difficulty: ENUM, challenge_type: ENUM, starter_code: TEXT, test_cases: JSONB, hints: JSONB, time_limit_seconds: INT(default 300), created_at: TIMESTAMP | belongs_to topic, has_many submissions | NOT NULL(title, problem_statement, test_cases) |
| design_exercise | id: UUID, topic_id: UUID, title: VARCHAR(300), prompt: TEXT, constraints: TEXT, evaluation_rubric: JSONB, reference_approach: TEXT, difficulty: ENUM, estimated_minutes: INT, created_at: TIMESTAMP | belongs_to topic | NOT NULL(title, prompt, evaluation_rubric) |

### User Progress Tables

| Entity | Fields | Relationships | Constraints |
|--------|--------|---------------|-------------|
| card_review | id: UUID, card_id: UUID, session_id: UUID, grade: INT, previous_interval: INT, new_interval: INT, previous_ease: DOUBLE, new_ease: DOUBLE, reviewed_at: TIMESTAMP | belongs_to card, belongs_to study_session | NOT NULL(card_id, grade), grade CHECK(1-5) |
| quiz_session | id: UUID, guide_id: UUID, started_at: TIMESTAMP, completed_at: TIMESTAMP, score: DOUBLE, total_questions: INT, correct_answers: INT | belongs_to guide, has_many quiz_answers | NOT NULL(guide_id) |
| quiz_answer | id: UUID, session_id: UUID, question_id: UUID, user_answer: TEXT, is_correct: BOOLEAN, answered_at: TIMESTAMP | belongs_to quiz_session, belongs_to quiz_question | NOT NULL(session_id, question_id) |
| submission | id: UUID, challenge_id: UUID, code: TEXT, status: ENUM, test_results: JSONB, ai_review: TEXT, execution_time_ms: INT, submitted_at: TIMESTAMP | belongs_to challenge | NOT NULL(challenge_id, code, status) |
| design_submission | id: UUID, exercise_id: UUID, answer: TEXT, ai_score: DOUBLE, ai_feedback: JSONB, submitted_at: TIMESTAMP | belongs_to design_exercise | NOT NULL(exercise_id, answer) |
| mock_interview | id: UUID, topic_area: ENUM, difficulty: ENUM, state: ENUM, overall_score: DOUBLE, category_scores: JSONB, started_at: TIMESTAMP, completed_at: TIMESTAMP | has_many interview_turns | NOT NULL(topic_area, state) |
| interview_turn | id: UUID, interview_id: UUID, turn_number: INT, ai_question: TEXT, user_answer: TEXT, evaluation: JSONB, phase: ENUM, asked_at: TIMESTAMP, answered_at: TIMESTAMP | belongs_to mock_interview | NOT NULL(interview_id, turn_number) |
| study_session | id: UUID, session_type: VARCHAR(50), topic_id: UUID, started_at: TIMESTAMP, ended_at: TIMESTAMP, items_completed: INT, score: DOUBLE | belongs_to topic | NOT NULL(session_type, started_at) |
| daily_activity | id: UUID, activity_date: DATE, cards_reviewed: INT, challenges_solved: INT, quizzes_completed: INT, interviews_done: INT, study_minutes: INT | — | UNIQUE(activity_date) |
| user_progress | id: UUID, topic_id: UUID, cards_mastered: INT, cards_total: INT, challenges_solved: INT, challenges_total: INT, guides_completed: INT, guides_total: INT, last_activity: TIMESTAMP | belongs_to topic | UNIQUE(topic_id) |

### JSONB Structures

```json
// challenge.test_cases
[
  {"input": "new int[]{1,2,3}", "expected": "6", "description": "Sum of positive array"},
  {"input": "new int[]{}", "expected": "0", "description": "Empty array"}
]

// challenge.hints
["Think about edge cases with empty input", "Consider using a running sum"]

// design_exercise.evaluation_rubric
{
  "categories": [
    {"name": "Scalability", "weight": 0.3, "criteria": ["Horizontal scaling", "Caching strategy"]},
    {"name": "Data Model", "weight": 0.25, "criteria": ["Schema design", "Indexing"]}
  ]
}

// interview_turn.evaluation
{"score": 7, "strengths": ["Clear structure"], "weaknesses": ["Missing edge case"], "follow_up_reason": "probe"}

// mock_interview.category_scores
{"technical_depth": 7.5, "communication": 8.0, "problem_solving": 6.5, "system_thinking": 7.0}
```

## API Endpoints

### Topics
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/topics | List root topics with children (tree) |
| GET | /api/topics/{id} | Topic detail with stats |
| GET | /api/topics/{id}/cards | Cards under this topic |
| GET | /api/topics/{id}/guides | Guides under this topic |
| GET | /api/topics/{id}/challenges | Challenges under this topic |

### Flashcards & Review
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/cards/due | Due cards count + next review time |
| GET | /api/cards/{id} | Single card detail with review history |
| POST | /api/reviews/start | Start review session (body: {topicId?, limit?}) |
| POST | /api/reviews/{sessionId}/grade | Grade card (body: {cardId, grade}) |
| GET | /api/reviews/{sessionId}/summary | Session results summary |
| POST | /api/cards/{id}/explain | AI-generate deeper explanation |

### Guides & Quizzes
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/guides/{id} | Guide content + linked quiz questions |
| POST | /api/quizzes/start | Start quiz (body: {guideId, count?}) |
| POST | /api/quizzes/{sessionId}/answer | Submit answer (body: {questionId, answer}) |
| GET | /api/quizzes/{sessionId}/results | Quiz session results |
| POST | /api/guides/{id}/generate-quiz | AI-generate questions from guide |

### Coding Dojo
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/challenges | List challenges (params: type, difficulty, page) |
| GET | /api/challenges/{id} | Challenge detail + user's past submissions |
| POST | /api/challenges/{id}/submit | Submit solution (body: {code}) |
| POST | /api/challenges/run | Quick-run code (body: {code}) — playground |
| GET | /api/challenges/{id}/hints/{n} | Reveal hint N for challenge |

### System Design & Mock Interviews
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/exercises | List design exercises (params: area, difficulty) |
| GET | /api/exercises/{id} | Exercise detail |
| POST | /api/exercises/{id}/submit | Submit design answer (body: {answer}) |
| POST | /api/interviews/start | Start mock interview (body: {topicArea, difficulty}) |
| POST | /api/interviews/{id}/answer | Submit answer to current question (body: {answer}) |
| POST | /api/interviews/{id}/end | End interview early, get scorecard |
| GET | /api/interviews/{id} | Full interview transcript + scores |

### Dashboard & Progress
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/dashboard | Today's focus: due cards, recommendations, streak |
| GET | /api/progress | Overall progress per topic area |
| GET | /api/progress/streak | Current streak + longest streak |
| GET | /api/progress/weak-areas | Topics with accuracy < 60% |
| GET | /api/progress/history | Study session history (params: since, limit) |

## Frontend Structure

```
src/
├── main.tsx
├── App.tsx                          # Router setup
├── api/
│   └── client.ts                    # Fetch-based API client
├── types/
│   └── index.ts                     # All TypeScript interfaces
├── components/
│   ├── Layout.tsx                   # App shell: sidebar + content
│   ├── TopicTree.tsx                # Hierarchical topic browser
│   ├── FlashCard.tsx                # Flip card with front/back
│   ├── GradeButtons.tsx             # 1-5 grade selection
│   ├── CodeEditor.tsx               # Monaco editor wrapper
│   ├── TestResultPanel.tsx          # Pass/fail test display
│   ├── AiFeedbackPanel.tsx          # AI review/evaluation display
│   ├── ChatBubble.tsx               # Interview conversation bubble
│   ├── Timer.tsx                    # Countdown timer component
│   ├── StreakBadge.tsx              # Streak counter with fire icon
│   ├── ProgressRing.tsx             # Circular progress indicator
│   ├── TopicHeatmap.tsx             # Topic mastery heatmap
│   ├── MarkdownRenderer.tsx         # Render guide content
│   └── DifficultyBadge.tsx          # Color-coded difficulty tag
├── pages/
│   ├── Dashboard.tsx                # Daily focus, streak, recommendations
│   ├── TopicBrowser.tsx             # Browse topic tree
│   ├── ReviewSession.tsx            # Flashcard review flow
│   ├── GuidePage.tsx                # Read guide + inline quizzes
│   ├── QuizSession.tsx              # Active quiz session
│   ├── ChallengeList.tsx            # Browse challenges
│   ├── ChallengeWorkspace.tsx       # Code editor + test results
│   ├── DesignExerciseList.tsx       # Browse design exercises
│   ├── DesignWorkspace.tsx          # Text editor + AI evaluation
│   ├── MockInterviewSession.tsx     # Chat-style interview UI
│   └── Progress.tsx                 # Full progress view
└── hooks/
    ├── useReviewSession.ts          # Review state management
    ├── useTimer.ts                  # Countdown/stopwatch hook
    └── useInterviewSession.ts       # Interview state + WebSocket
```

## Content Seeding Design

### YAML Card Format

```yaml
# seed/cards/java-core/collections.yaml
topic: "Java Core/Collections"
cards:
  - front: "What is the time complexity of HashMap.get()?"
    back: "O(1) average case, O(n) worst case (hash collisions)"
    code_snippet: |
      Map<String, Integer> map = new HashMap<>();
      map.put("key", 42);
      int value = map.get("key"); // O(1)
    explanation: "HashMap uses bucket array with linked lists/trees for collisions..."
    difficulty: MEDIUM
    tags: [collections, hashmap, complexity]

  - front: "Difference between ArrayList and LinkedList?"
    back: "ArrayList: O(1) random access, O(n) insert. LinkedList: O(n) access, O(1) insert at known position."
    difficulty: EASY
    tags: [collections, list]
```

### YAML Challenge Format

```yaml
# seed/challenges/dsa/two-sum.yaml
topic: "DSA/Arrays"
challenges:
  - title: "Two Sum"
    difficulty: EASY
    type: DSA
    time_limit_seconds: 300
    problem_statement: |
      Given an array of integers `nums` and a target integer `target`,
      return indices of the two numbers that add up to `target`.
      You may assume each input has exactly one solution.
    starter_code: |
      public int[] twoSum(int[] nums, int target) {
          // Your solution here
      }
    test_cases:
      - input: "new int[]{2, 7, 11, 15}"
        args: "9"
        expected: "[0, 1]"
        description: "Basic case"
      - input: "new int[]{3, 2, 4}"
        args: "6"
        expected: "[1, 2]"
        description: "Non-adjacent elements"
    hints:
      - "Think about what complement you need for each number"
      - "A HashMap can store seen numbers and their indices"
      - "Single pass: for each num, check if (target - num) exists in map"
```

### YAML Guide Format

```yaml
# seed/guides/spring-boot/dependency-injection.yaml
topic: "Spring Boot/Core Concepts"
guides:
  - title: "Dependency Injection in Spring"
    estimated_minutes: 15
    content: |
      # Dependency Injection in Spring

      ## Constructor Injection (Preferred)

      ```java
      @Service
      public class OrderService {
          private final PaymentGateway gateway;
          private final OrderRepository repo;

          public OrderService(PaymentGateway gateway, OrderRepository repo) {
              this.gateway = gateway;
              this.repo = repo;
          }
      }
      ```

      ## Why Constructor Injection?
      - Immutable dependencies (final fields)
      - Fail-fast: missing dependency = startup error
      - Testable: easy to pass mocks

      ## Quiz Checkpoint
      <!-- quiz: di-basics -->
```

### Seed Loader Pipeline

| Step | Action | Detail |
|------|--------|--------|
| 1 | Scan classpath | Find all `seed/**/*.yaml` files |
| 2 | Parse YAML | Deserialize into seed DTOs (SnakeYAML) |
| 3 | Resolve topics | Match `topic: "Java Core/Collections"` → find or create topic chain |
| 4 | Upsert content | Insert if not exists (deduplicate by title + topic) |
| 5 | Report | Log: "Seeded 120 cards, 40 challenges, 30 guides" |

Runs conditionally: `@ConditionalOnProperty(name = "interviewkata.seed.enabled", havingValue = "true")`. Safe for re-runs (idempotent via title+topic uniqueness).

## Security: JShell Sandboxing

### Threat Model

| Threat | Mitigation |
|--------|-----------|
| File system access | Custom SecurityManager denying FilePermission |
| Network access | Deny SocketPermission, NetPermission |
| Process spawning | Deny RuntimePermission("exec") |
| Reflection abuse | Deny ReflectPermission |
| Infinite loops | 5-second hard timeout via process kill |
| Memory exhaustion | -Xmx256m JVM flag on sandbox process |
| System.exit() | Deny RuntimePermission("exitVM.*") |
| Class loading exploits | Restricted classpath (JDK stdlib only) |

### Sandbox Architecture

```
┌─────────────────────────────┐
│      Main Application       │
│      (Spring Boot JVM)      │
│                             │
│  ChallengeService           │
│    └── JShellSandbox        │
│          │                  │
└──────────┼──────────────────┘
           │ ProcessBuilder
           │ (fork + exec)
           ▼
┌─────────────────────────────┐
│    Sandbox Process (JVM)    │
│    -Xmx256m                 │
│    -Djava.security.manager  │
│    -Djava.security.policy=  │
│       sandbox.policy        │
│                             │
│  ┌─────────────────────┐   │
│  │      JShell          │   │
│  │  (restricted env)    │   │
│  │  - no file I/O       │   │
│  │  - no network        │   │
│  │  - no reflection     │   │
│  │  - no exec           │   │
│  │  - stdlib only       │   │
│  └─────────────────────┘   │
│                             │
│  stdout/stderr → pipe       │
└─────────────────────────────┘
     ↑ killed after 5s timeout
```

### Implementation Notes

- **Process isolation**: Each execution spawns a new JVM process (not thread-level isolation). Expensive but secure. Pool 2-3 warm processes for responsiveness.
- **SecurityManager deprecation (Java 17+)**: SecurityManager deprecated in Java 17, removed for removal in 21. Alternative approach: use ProcessBuilder with restricted classpath + module system `--limit-modules java.base` to restrict available APIs. Combine with `--add-reads` whitelist for safe modules only.
- **Timeout**: `Process.waitFor(5, TimeUnit.SECONDS)` → `destroyForcibly()` if exceeded.
- **Output capture**: Redirect stdout/stderr to `ByteArrayOutputStream` via process streams. Cap at 10KB output.
- **Module-based restriction (preferred over SecurityManager)**:
  ```
  java --limit-modules java.base
       --add-modules jdk.jshell
       -Xmx256m
       -XX:+UseSerialGC
  ```
  This prevents `java.net`, `java.io` (file ops), `java.lang.reflect` deep access.

## Mock Interview State Machine

```
              ┌──────────┐
              │  CREATED  │
              └─────┬─────┘
                    │ startInterview()
                    ▼
              ┌──────────┐
         ┌───►│  ASKING   │◄────────────────────┐
         │    └─────┬─────┘                      │
         │          │ user submits answer         │
         │          ▼                             │
         │    ┌──────────┐                        │
         │    │ EVALUATING│ (AI evaluates)        │
         │    └─────┬─────┘                        │
         │          │                             │
         │    ┌─────┴──────────────┐              │
         │    │                    │              │
         │    ▼ score >= 7         ▼ score < 7    │
         │  ┌──────────┐   ┌──────────┐          │
         │  │ ADVANCE   │   │  PROBE   │          │
         │  └─────┬─────┘   └─────┬────┘          │
         │        │                │              │
         │        │ next phase     │ deeper Q     │
         │        │                └──────────────┘
         │        ▼
         │  ┌──────────────┐
         │  │ phase++      │
         │  │ (INTRO→TECH  │
         │  │  →DEEP_DIVE  │
         │  │  →WRAP_UP)   │
         │  └──────┬───────┘
         │         │
         │    ┌────┴─────┐
         │    │ last      │ not last
         │    │ phase?    ├─────────────┘
         │    └────┬──────┘
         │         │ yes
         │         ▼
         │   ┌──────────┐
         └───│ COMPLETE  │
             └──────────┘
                    │ generateScorecard()
                    ▼
             ┌──────────┐
             │  SCORED   │
             └──────────┘
```

**Phase progression**: INTRO (1 question) → TECHNICAL (3-5 questions) → DEEP_DIVE (2-3 questions) → WRAP_UP (1 question). Total: 7-10 turns max.

**Probing**: If user scores < 7 on a turn, AI asks follow-up on the same sub-topic (max 2 probes per question). After 2 probes, advance regardless.

## Decisions

| Decision | Choice | Reason | Alternatives | Tradeoffs |
|----------|--------|--------|--------------|-----------|
| Code execution | Process-forked JShell | True isolation, no shared memory | Thread-based sandbox, Docker containers, GraalJS | Slower startup (mitigated by warm pool), but maximum security |
| AI framework | Spring AI | Native Spring integration, provider abstraction, structured output | Direct HTTP to Anthropic/OpenAI, LangChain4j | Tied to Spring AI maturity; but matches project stack |
| Spaced repetition | SM-2 (original) | Simple, proven, well-documented | FSRS, SM-5, Leitner system | Less optimal than FSRS but simpler to implement and debug |
| Content format | YAML seed files | Human-readable, diffable, easy to author | JSON, database seeder scripts, admin UI | Requires restart to re-seed; but content is stable |
| Auth | Simple shared secret token | Single user, no registration, minimal overhead | Spring Security + JWT, OAuth2, Basic Auth | No session management; token in env var, validated per request |
| Frontend state | useState + useEffect | Adequate for this scale, no external deps | Redux, Zustand, React Query | Re-fetch on nav (acceptable for single user, low data volume) |
| DB for sessions | PostgreSQL (same DB) | Simple, no extra infra, JSONB for flexible fields | Redis for sessions, separate analytics DB | All in one DB; acceptable at single-user scale |
| Mock interview limit | 3/day hardcoded | Control AI costs, prevent abuse | Configurable via properties, token budget tracking | Simple; can relax later via config |

## Risks

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| JShell sandbox escape | Remote code execution on host | Low | Process isolation + module restriction + no file/net. Audit with known exploits. |
| AI cost spike | Unexpected billing from mock interviews + code review | Medium | Rate limit interviews (3/day), cache AI explanations, lazy-load AI review (opt-in) |
| SecurityManager removal in Java 21 | Primary sandboxing mechanism unavailable | High (already deprecated) | Use module-system restriction (`--limit-modules`) as primary; SecurityManager as defense-in-depth only |
| Stale seed content | Cards/challenges become outdated | Low | Version seed files, add `updated_at` tracking, thumbs-up/down quality signals |
| JShell cold start latency | First submission slow (JVM startup) | Medium | Maintain warm pool of 2-3 pre-started sandbox processes |
| AI hallucination in evaluations | Incorrect grading/feedback misleads user | Medium | Include rubric in all evaluation prompts, show confidence score, allow user to flag |
| Large transcript context window | Mock interview transcript exceeds AI context | Low | Summarize earlier turns, keep last 5 turns verbatim + summary prefix |
| SM-2 ease factor death spiral | Card stuck at minimum ease (1.3), too frequent | Low | Floor ease at 1.3, add "reset card" action, graduating interval prevents permanent loop |

## Test Plan

### Unit Tests

**SM2Scheduler**:
- Grade 5 (perfect): interval doubles, ease increases
- Grade 3 (pass): interval advances at current ease
- Grade 1 (fail): reset to learning (interval=1, repetitions=0)
- Ease factor floor: never drops below 1.3
- New card: first review always 1 day
- Graduation: card with interval > 21 days → GRADUATED status

**ContentSeeder**:
- Parse valid YAML → correct domain objects
- Duplicate detection: same title+topic → skip, not error
- Missing required field → meaningful error with file/line
- Topic path resolution: "Java Core/Collections" → creates hierarchy

**AiService**:
- Mock AI responses → verify prompt construction includes rubric
- Evaluation parsing: structured response → EvaluationResult mapping
- Provider unavailable → AiServiceException with retry suggestion
- Interview conductor: transcript context correctly truncated at limit

**MockInterviewEngine**:
- State transitions: ASKING → EVALUATING → ADVANCE/PROBE
- Rate limit: 4th interview in same day → RateLimitException
- Probe limit: max 2 probes per question → advance on 3rd
- Phase progression: correct number of questions per phase
- End early: partial scorecard generated from available turns

### Integration Tests

**JShell Sandbox** (Testcontainers or local process):
- Valid code → correct stdout capture
- Infinite loop → timeout + process killed within 6s
- File access attempt → SecurityException or no permission
- Network access attempt → denied
- Memory exhaustion → OOM killed, error returned
- Multiple sequential executions → no state leakage between runs

**Review Session Flow**:
- Start session → get due cards → grade all → verify next_review dates
- No due cards → empty session response
- Mixed grades → each card gets correct new interval

**Challenge Submission**:
- All tests pass → PASSED status
- Partial pass → FAILED with details per test case
- Compilation error → ERROR status with compiler message

### End-to-End Tests

**Daily Review Flow**:
1. Seed 5 cards with next_review = today
2. Start session → receive 5 cards
3. Grade each (mix of 3s and 5s)
4. Verify: cards with grade=5 have longer next interval
5. Dashboard shows updated streak + "0 cards due"

**Code Challenge Flow**:
1. Load a seeded challenge
2. Submit correct solution → all tests pass
3. Submit solution with bug → partial failure with test details
4. Verify submission history recorded

**Mock Interview Flow**:
1. Start interview for "System Design" + MEDIUM
2. Answer 3 questions
3. End interview → get scorecard with category scores
4. Verify 3 InterviewTurn records in DB

### Non-Functional Tests

**Performance**:
- Review session start: < 100ms for 50 due cards query
- Challenge submission: < 7s total (5s sandbox + 2s overhead)
- AI evaluation: < 10s (depends on provider)
- Dashboard load: < 200ms

**Security**:
- JShell: verify file/network/reflection/exec all denied
- Auth: unauthenticated request → 401
- Auth: invalid token → 401
- Input validation: oversized code submission (>100KB) → 400

## Configuration Design

```yaml
# application.yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/interviewkata
    username: interviewkata
    password: ${INTERVIEWKATA_DB_PASSWORD:interviewkata}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
  ai:
    anthropic:
      api-key: ${INTERVIEWKATA_AI_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-20250514
          max-tokens: 4096
    # Alternatively for OpenAI:
    # openai:
    #   api-key: ${INTERVIEWKATA_AI_API_KEY}
    #   chat:
    #     options:
    #       model: gpt-4o

# Application-specific config
interviewkata:
  auth:
    token: ${INTERVIEWKATA_AUTH_TOKEN:dev-token}
  sandbox:
    timeout-ms: 5000
    max-heap-mb: 256
    warm-pool-size: 2
    max-output-bytes: 10240
    allowed-modules:
      - java.base
      - jdk.jshell
  ai:
    provider: anthropic  # or openai
    max-interviews-per-day: 3
    cache-explanations: true
    evaluation-model: claude-sonnet-4-20250514
    generation-model: claude-sonnet-4-20250514
  seed:
    enabled: true
    path: classpath:seed/
  review:
    default-session-size: 20
    graduating-interval-days: 21
    learning-steps-minutes: [1, 10]
    initial-ease-factor: 2.5
    minimum-ease-factor: 1.3
  progress:
    streak-reset-hour: 4  # 4 AM local time
    weak-area-threshold: 0.6
```

### Environment Variables

| Variable | Purpose | Required |
|----------|---------|----------|
| INTERVIEWKATA_DB_PASSWORD | PostgreSQL password | Yes (prod) |
| INTERVIEWKATA_AI_API_KEY | Anthropic/OpenAI API key | Yes |
| INTERVIEWKATA_AUTH_TOKEN | Simple auth bearer token | Yes (prod) |
| INTERVIEWKATA_AI_PROVIDER | "anthropic" or "openai" | No (default: anthropic) |

## Liquibase Migration Plan

```
db/changelog/
├── db.changelog-master.yaml          # Includes all changelogs
├── 001-create-topics.yaml            # topic table + seed root topics
├── 002-create-cards.yaml             # card + card_review tables
├── 003-create-guides.yaml            # guide + quiz_question tables
├── 004-create-quizzes.yaml           # quiz_session + quiz_answer tables
├── 005-create-challenges.yaml        # challenge + submission tables
├── 006-create-design-exercises.yaml  # design_exercise + design_submission
├── 007-create-mock-interviews.yaml   # mock_interview + interview_turn
├── 008-create-progress.yaml          # user_progress + daily_activity + study_session
├── 009-seed-topic-hierarchy.yaml     # INSERT root topics for all 6 areas
└── 010-create-indexes.yaml           # Performance indexes
```

### Key Indexes

```sql
CREATE INDEX idx_card_next_review ON card(next_review) WHERE status != 'GRADUATED';
CREATE INDEX idx_card_topic ON card(topic_id);
CREATE INDEX idx_submission_challenge ON submission(challenge_id);
CREATE INDEX idx_daily_activity_date ON daily_activity(activity_date);
CREATE INDEX idx_interview_turn_interview ON interview_turn(interview_id, turn_number);
CREATE INDEX idx_mock_interview_date ON mock_interview(started_at);
CREATE INDEX idx_card_review_card ON card_review(card_id, reviewed_at DESC);
```
