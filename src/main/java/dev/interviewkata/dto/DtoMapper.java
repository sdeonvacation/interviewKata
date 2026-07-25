package dev.interviewkata.dto;

import dev.interviewkata.model.*;

import java.util.List;

/**
 * Stateless utility for converting entities to DTOs.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static TopicDto toDto(Topic topic, int childCount, int cardCount) {
        return new TopicDto(
                topic.getId(),
                topic.getName(),
                topic.getArea(),
                topic.getParent() != null ? topic.getParent().getId() : null,
                topic.getDescription(),
                topic.getSortOrder(),
                childCount,
                cardCount
        );
    }

    public static CardDto toDto(Card card) {
        return new CardDto(
                card.getId(),
                card.getTopic().getId(),
                card.getTopic().getName(),
                card.getFront(),
                card.getBack(),
                card.getCodeSnippet(),
                card.getExplanation(),
                card.getDifficulty(),
                card.getTags(),
                card.getStatus(),
                card.getNextReview()
        );
    }

    public static GuideDto toDto(Guide guide, int questionCount) {
        return new GuideDto(
                guide.getId(),
                guide.getTopic().getId(),
                guide.getTitle(),
                guide.getContentMarkdown(),
                guide.getEstimatedMinutes(),
                questionCount
        );
    }

    public static QuizQuestionDto toDto(QuizQuestion question) {
        return new QuizQuestionDto(
                question.getId(),
                question.getQuestionType(),
                question.getQuestionText(),
                question.getOptions(),
                question.getDifficulty()
        );
    }

    public static ChallengeDto toDto(Challenge challenge, boolean solved) {
        return new ChallengeDto(
                challenge.getId(),
                challenge.getTopic().getId(),
                challenge.getTitle(),
                challenge.getDifficulty(),
                challenge.getChallengeType(),
                solved
        );
    }

    public static ChallengeDetailDto toDetailDto(Challenge challenge, List<SubmissionResultDto> submissions) {
        return new ChallengeDetailDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getProblemStatement(),
                challenge.getDifficulty(),
                challenge.getChallengeType(),
                challenge.getStarterCode(),
                challenge.getHints(),
                challenge.getTimeLimitSeconds(),
                submissions
        );
    }

    public static SubmissionResultDto toDto(Submission submission) {
        return new SubmissionResultDto(
                submission.getId(),
                submission.getStatus(),
                submission.getTestResults(),
                submission.getAiReview(),
                submission.getExecutionTimeMs()
        );
    }

    public static DesignExerciseDto toDto(DesignExercise exercise) {
        return new DesignExerciseDto(
                exercise.getId(),
                exercise.getTopic().getId(),
                exercise.getTitle(),
                exercise.getDifficulty(),
                exercise.getEstimatedMinutes()
        );
    }

    public static InterviewTurnDto toDto(InterviewTurn turn) {
        return new InterviewTurnDto(
                turn.getTurnNumber(),
                turn.getAiQuestion(),
                turn.getEvaluation(),
                turn.getPhase(),
                turn.getAnsweredAt() != null
        );
    }

    public static DashboardDto.DailyActivityDto toDto(DailyActivity activity) {
        return new DashboardDto.DailyActivityDto(
                activity.getCardsReviewed(),
                activity.getChallengesSolved(),
                activity.getQuizzesCompleted(),
                activity.getInterviewsDone(),
                activity.getStudyMinutes()
        );
    }

    public static DashboardDto.RecentSessionDto toDto(StudySession session) {
        return new DashboardDto.RecentSessionDto(
                session.getSessionType(),
                session.getTopic() != null ? session.getTopic().getName() : null,
                session.getItemsCompleted(),
                session.getScore()
        );
    }
}
