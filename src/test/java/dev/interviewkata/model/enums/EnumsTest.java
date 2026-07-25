package dev.interviewkata.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumsTest {

    @Test
    void topicArea_hasAllValues() {
        assertThat(TopicArea.values()).containsExactly(
                TopicArea.JAVA_CORE, TopicArea.SPRING_BOOT, TopicArea.SYSTEM_DESIGN,
                TopicArea.DSA, TopicArea.DATABASE, TopicArea.ARCHITECTURE, TopicArea.BEHAVIORAL
        );
    }

    @Test
    void cardStatus_hasAllValues() {
        assertThat(CardStatus.values()).containsExactly(
                CardStatus.NEW, CardStatus.LEARNING, CardStatus.REVIEW, CardStatus.GRADUATED
        );
    }

    @Test
    void difficulty_hasAllValues() {
        assertThat(Difficulty.values()).containsExactly(
                Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD
        );
    }

    @Test
    void questionType_hasAllValues() {
        assertThat(QuestionType.values()).containsExactly(
                QuestionType.MCQ, QuestionType.FILL_BLANK,
                QuestionType.PREDICT_OUTPUT, QuestionType.EXPLAIN_CODE
        );
    }

    @Test
    void challengeType_hasAllValues() {
        assertThat(ChallengeType.values()).containsExactly(
                ChallengeType.DSA, ChallengeType.JAVA, ChallengeType.SQL
        );
    }

    @Test
    void submissionStatus_hasAllValues() {
        assertThat(SubmissionStatus.values()).containsExactly(
                SubmissionStatus.RUNNING, SubmissionStatus.PASSED,
                SubmissionStatus.FAILED, SubmissionStatus.TIMEOUT, SubmissionStatus.ERROR
        );
    }

    @Test
    void interviewState_hasAllValues() {
        assertThat(InterviewState.values()).containsExactly(
                InterviewState.ASKING, InterviewState.WAITING,
                InterviewState.FOLLOW_UP, InterviewState.PROBING, InterviewState.COMPLETE
        );
    }

    @Test
    void interviewPhase_hasAllValues() {
        assertThat(InterviewPhase.values()).containsExactly(
                InterviewPhase.INTRO, InterviewPhase.TECHNICAL,
                InterviewPhase.DEEP_DIVE, InterviewPhase.WRAP_UP,
                InterviewPhase.QUESTION, InterviewPhase.PROBE, InterviewPhase.FOLLOW_UP
        );
    }

    @ParameterizedTest
    @EnumSource(TopicArea.class)
    void topicArea_valueOfRoundTrips(TopicArea value) {
        assertThat(TopicArea.valueOf(value.name())).isEqualTo(value);
    }

    @ParameterizedTest
    @EnumSource(CardStatus.class)
    void cardStatus_valueOfRoundTrips(CardStatus value) {
        assertThat(CardStatus.valueOf(value.name())).isEqualTo(value);
    }

    @Test
    void invalidEnumValue_throwsException() {
        assertThatThrownBy(() -> TopicArea.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
