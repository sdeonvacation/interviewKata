package dev.interviewkata.service;

import dev.interviewkata.dto.QuizResultDto;
import dev.interviewkata.model.QuizAnswer;
import dev.interviewkata.model.QuizQuestion;
import dev.interviewkata.model.QuizSession;
import dev.interviewkata.repository.QuizAnswerRepository;
import dev.interviewkata.repository.QuizQuestionRepository;
import dev.interviewkata.repository.QuizSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    public QuizService(QuizSessionRepository quizSessionRepository,
                       QuizQuestionRepository quizQuestionRepository,
                       QuizAnswerRepository quizAnswerRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAnswerRepository = quizAnswerRepository;
    }

    @Transactional
    public QuizSession startQuiz(UUID guideId, int count) {
        List<QuizQuestion> questions = quizQuestionRepository.findByGuideId(guideId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions available for guide: " + guideId);
        }

        Collections.shuffle(questions);
        int questionCount = Math.min(count > 0 ? count : 10, questions.size());

        QuizSession session = QuizSession.builder()
                .guideId(guideId)
                .totalQuestions(questionCount)
                .correctAnswers(0)
                .build();

        return quizSessionRepository.save(session);
    }

    @Transactional
    public boolean submitAnswer(UUID sessionId, UUID questionId, String answer) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz session not found: " + sessionId));
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));

        boolean correct = question.getCorrectAnswer().equalsIgnoreCase(answer.trim());

        QuizAnswer quizAnswer = QuizAnswer.builder()
                .session(session)
                .question(question)
                .userAnswer(answer)
                .isCorrect(correct)
                .build();
        quizAnswerRepository.save(quizAnswer);

        if (correct) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            quizSessionRepository.save(session);
        }

        return correct;
    }

    public QuizResultDto getResults(UUID sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz session not found: " + sessionId));

        List<QuizAnswer> answers = quizAnswerRepository.findBySessionId(sessionId);
        int correct = (int) answers.stream().filter(QuizAnswer::isCorrect).count();
        double score = session.getTotalQuestions() > 0
                ? (double) correct / session.getTotalQuestions() * 100.0
                : 0.0;

        // Mark session complete if all answered
        if (answers.size() >= session.getTotalQuestions() && session.getCompletedAt() == null) {
            session.setCompletedAt(LocalDateTime.now());
            session.setScore(score);
            quizSessionRepository.save(session);
        }

        return new QuizResultDto(sessionId, session.getTotalQuestions(), correct, score);
    }
}
