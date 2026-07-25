package dev.interviewkata.model;

import dev.interviewkata.model.enums.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityBuilderTest {

    @Test
    void guide_builder() {
        Guide guide = Guide.builder()
                .topic(topic())
                .title("Spring Boot Basics")
                .contentMarkdown("# Intro")
                .sortOrder(1)
                .estimatedMinutes(30)
                .build();

        assertThat(guide.getTitle()).isEqualTo("Spring Boot Basics");
        assertThat(guide.getEstimatedMinutes()).isEqualTo(30);
    }

    @Test
    void quizQuestion_builder() {
        QuizQuestion q = QuizQuestion.builder()
                .topic(topic())
                .questionType(QuestionType.MCQ)
                .questionText("What is DI?")
                .options(List.of(Map.of("A", "Dependency Injection", "B", "Data Integration")))
                .correctAnswer("A")
                .difficulty(Difficulty.EASY)
                .aiGenerated(true)
                .build();

        assertThat(q.getQuestionType()).isEqualTo(QuestionType.MCQ);
        assertThat(q.isAiGenerated()).isTrue();
        assertThat(q.getOptions()).hasSize(1);
    }

    @Test
    void quizSession_builder() {
        QuizSession s = QuizSession.builder()
                .guideId(UUID.randomUUID())
                .totalQuestions(10)
                .correctAnswers(7)
                .build();

        assertThat(s.getTotalQuestions()).isEqualTo(10);
        assertThat(s.getCompletedAt()).isNull();
        assertThat(s.getScore()).isNull();
    }

    @Test
    void quizAnswer_builder() {
        QuizAnswer a = QuizAnswer.builder()
                .session(QuizSession.builder().totalQuestions(5).correctAnswers(3).build())
                .question(QuizQuestion.builder()
                        .topic(topic())
                        .questionType(QuestionType.FILL_BLANK)
                        .questionText("Q")
                        .correctAnswer("A")
                        .difficulty(Difficulty.MEDIUM)
                        .aiGenerated(false)
                        .build())
                .userAnswer("my answer")
                .isCorrect(true)
                .answeredAt(LocalDateTime.now())
                .build();

        assertThat(a.getUserAnswer()).isEqualTo("my answer");
        assertThat(a.isCorrect()).isTrue();
    }

    @Test
    void challenge_builderDefaults() {
        Challenge c = Challenge.builder()
                .topic(topic())
                .title("Two Sum")
                .problemStatement("Find two numbers...")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        assertThat(c.getTimeLimitSeconds()).isEqualTo(300);
        assertThat(c.getTestCases()).isEmpty();
        assertThat(c.getHints()).isEmpty();
        assertThat(c.getStarterCode()).isNull();
    }

    @Test
    void submission_builder() {
        Submission s = Submission.builder()
                .challenge(Challenge.builder()
                        .topic(topic())
                        .title("T")
                        .problemStatement("P")
                        .difficulty(Difficulty.HARD)
                        .challengeType(ChallengeType.JAVA)
                        .build())
                .code("class Solution {}")
                .status(SubmissionStatus.PASSED)
                .testResults(List.of(Map.of("name", "test1", "passed", true)))
                .submittedAt(LocalDateTime.now())
                .build();

        assertThat(s.getStatus()).isEqualTo(SubmissionStatus.PASSED);
        assertThat(s.getTestResults()).hasSize(1);
        assertThat(s.getAiReview()).isNull();
        assertThat(s.getExecutionTimeMs()).isNull();
    }

    @Test
    void designExercise_builder() {
        DesignExercise de = DesignExercise.builder()
                .topic(topic())
                .title("Design a Cache")
                .prompt("Design an LRU cache")
                .constraints("O(1) operations")
                .evaluationRubric(Map.of("correctness", 40, "scalability", 30))
                .difficulty(Difficulty.HARD)
                .estimatedMinutes(45)
                .build();

        assertThat(de.getEvaluationRubric()).containsKey("correctness");
        assertThat(de.getReferenceApproach()).isNull();
    }

    @Test
    void designSubmission_builder() {
        DesignSubmission ds = DesignSubmission.builder()
                .exercise(DesignExercise.builder()
                        .topic(topic())
                        .title("T")
                        .prompt("P")
                        .constraints("C")
                        .evaluationRubric(Map.of())
                        .difficulty(Difficulty.MEDIUM)
                        .estimatedMinutes(30)
                        .build())
                .answer("My design approach...")
                .submittedAt(LocalDateTime.now())
                .build();

        assertThat(ds.getAiScore()).isNull();
        assertThat(ds.getAiFeedback()).isNull();
    }

    @Test
    void mockInterview_builder() {
        MockInterview mi = MockInterview.builder()
                .topicArea(TopicArea.SYSTEM_DESIGN)
                .difficulty(Difficulty.HARD)
                .state(InterviewState.ASKING)
                .startedAt(LocalDateTime.now())
                .build();

        assertThat(mi.getOverallScore()).isNull();
        assertThat(mi.getCategoryScores()).isNull();
        assertThat(mi.getCompletedAt()).isNull();
    }

    @Test
    void interviewTurn_builder() {
        MockInterview interview = MockInterview.builder()
                .topicArea(TopicArea.JAVA_CORE)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .startedAt(LocalDateTime.now())
                .build();

        InterviewTurn turn = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(1)
                .aiQuestion("Explain HashMap internals")
                .phase(InterviewPhase.TECHNICAL)
                .askedAt(LocalDateTime.now())
                .build();

        assertThat(turn.getTurnNumber()).isEqualTo(1);
        assertThat(turn.getPhase()).isEqualTo(InterviewPhase.TECHNICAL);
        assertThat(turn.getUserAnswer()).isNull();
        assertThat(turn.getEvaluation()).isNull();
        assertThat(turn.getAnsweredAt()).isNull();
    }

    @Test
    void studySession_builder() {
        StudySession ss = StudySession.builder()
                .sessionType("flashcard")
                .topic(topic())
                .startedAt(LocalDateTime.now())
                .itemsCompleted(15)
                .build();

        assertThat(ss.getSessionType()).isEqualTo("flashcard");
        assertThat(ss.getEndedAt()).isNull();
        assertThat(ss.getScore()).isNull();
    }

    @Test
    void dailyActivity_builderDefaults() {
        DailyActivity da = DailyActivity.builder()
                .activityDate(LocalDate.now())
                .build();

        assertThat(da.getCardsReviewed()).isZero();
        assertThat(da.getChallengesSolved()).isZero();
        assertThat(da.getQuizzesCompleted()).isZero();
        assertThat(da.getInterviewsDone()).isZero();
        assertThat(da.getStudyMinutes()).isZero();
    }

    @Test
    void userProgress_builderDefaults() {
        UserProgress up = UserProgress.builder()
                .topic(topic())
                .build();

        assertThat(up.getCardsMastered()).isZero();
        assertThat(up.getCardsTotal()).isZero();
        assertThat(up.getChallengesSolved()).isZero();
        assertThat(up.getChallengesTotal()).isZero();
        assertThat(up.getGuidesCompleted()).isZero();
        assertThat(up.getGuidesTotal()).isZero();
        assertThat(up.getLastActivity()).isNull();
    }

    private Topic topic() {
        return Topic.builder().name("Test Topic").area(TopicArea.JAVA_CORE).build();
    }
}
