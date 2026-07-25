package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.InterviewTurnDto;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.InterviewPhase;
import dev.interviewkata.model.enums.InterviewState;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.InterviewTurnRepository;
import dev.interviewkata.repository.MockInterviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MockInterviewEngine {

    private static final Logger log = LoggerFactory.getLogger(MockInterviewEngine.class);

    private final MockInterviewRepository mockInterviewRepository;
    private final InterviewTurnRepository interviewTurnRepository;
    private final AiService aiService;
    private final int maxInterviewsPerDay;

    public MockInterviewEngine(MockInterviewRepository mockInterviewRepository,
                               InterviewTurnRepository interviewTurnRepository,
                               AiService aiService,
                               @Value("${interviewkata.ai.max-interviews-per-day:3}") int maxInterviewsPerDay) {
        this.mockInterviewRepository = mockInterviewRepository;
        this.interviewTurnRepository = interviewTurnRepository;
        this.aiService = aiService;
        this.maxInterviewsPerDay = maxInterviewsPerDay;
    }

    @Transactional
    public MockInterview startInterview(TopicArea topicArea, Difficulty difficulty) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = mockInterviewRepository.countByStartedAtAfter(todayStart);

        if (todayCount >= maxInterviewsPerDay) {
            throw new RateLimitExceededException(
                    "Daily interview limit reached (" + maxInterviewsPerDay + "/day)");
        }

        MockInterview interview = MockInterview.builder()
                .topicArea(topicArea)
                .difficulty(difficulty)
                .state(InterviewState.ASKING)
                .build();
        MockInterview saved = mockInterviewRepository.save(interview);

        // Generate first question using appropriate prompt for topic type
        String firstQuestion = generateQuestion("", topicArea, InterviewPhase.INTRO);

        InterviewTurn firstTurn = InterviewTurn.builder()
                .interview(saved)
                .turnNumber(1)
                .aiQuestion(firstQuestion)
                .phase(InterviewPhase.INTRO)
                .build();
        interviewTurnRepository.save(firstTurn);

        return saved;
    }

    @Transactional
    public InterviewTurn submitAnswer(UUID interviewId, String answer) {
        MockInterview interview = mockInterviewRepository.findById(interviewId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found: " + interviewId));

        if (interview.getState() == InterviewState.COMPLETE) {
            throw new IllegalStateException("Interview is already complete");
        }

        List<InterviewTurn> turns = interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId);
        InterviewTurn lastTurn = turns.get(turns.size() - 1);

        // Record user answer on current turn
        lastTurn.setUserAnswer(answer);
        lastTurn.setAnsweredAt(LocalDateTime.now());
        interviewTurnRepository.save(lastTurn);

        // Determine next phase based on topic type
        int nextTurnNumber = turns.size() + 1;
        InterviewPhase nextPhase = interview.getTopicArea() == TopicArea.BEHAVIORAL
                ? determineBehavioralPhase(nextTurnNumber)
                : determinePhase(nextTurnNumber);

        // Build transcript from all turns for context
        String transcript = buildTranscript(turns);
        String nextQuestion = generateQuestion(transcript, interview.getTopicArea(), nextPhase);

        InterviewTurn nextTurn = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(nextTurnNumber)
                .aiQuestion(nextQuestion)
                .phase(nextPhase)
                .build();

        interview.setState(InterviewState.ASKING);
        mockInterviewRepository.save(interview);

        return interviewTurnRepository.save(nextTurn);
    }

    @Transactional
    public MockInterview endInterview(UUID interviewId) {
        MockInterview interview = mockInterviewRepository.findById(interviewId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found: " + interviewId));

        interview.setState(InterviewState.COMPLETE);
        interview.setCompletedAt(LocalDateTime.now());
        return mockInterviewRepository.save(interview);
    }

    public MockInterview getInterview(UUID interviewId) {
        return mockInterviewRepository.findById(interviewId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found: " + interviewId));
    }

    public List<InterviewTurnDto> getTurns(UUID interviewId) {
        List<InterviewTurn> turns = interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId);
        return turns.stream()
                .map(t -> new InterviewTurnDto(t.getTurnNumber(), t.getAiQuestion(), t.getEvaluation(), t.getPhase(), false))
                .toList();
    }

    private InterviewPhase determinePhase(int turnNumber) {
        if (turnNumber <= 2) return InterviewPhase.INTRO;
        if (turnNumber <= 5) return InterviewPhase.TECHNICAL;
        if (turnNumber <= 8) return InterviewPhase.DEEP_DIVE;
        return InterviewPhase.WRAP_UP;
    }

    private InterviewPhase determineBehavioralPhase(int turnNumber) {
        if (turnNumber <= 1) return InterviewPhase.INTRO;
        if (turnNumber <= 3) return InterviewPhase.QUESTION;
        if (turnNumber <= 5) return InterviewPhase.PROBE;
        if (turnNumber <= 7) return InterviewPhase.FOLLOW_UP;
        return InterviewPhase.WRAP_UP;
    }

    private String generateQuestion(String transcript, TopicArea topicArea, InterviewPhase phase) {
        if (topicArea == TopicArea.BEHAVIORAL) {
            return aiService.conductBehavioralInterview(transcript, topicArea.name(), phase.name());
        }
        return aiService.conductInterview(transcript, topicArea.name(), phase.name());
    }

    private String buildTranscript(List<InterviewTurn> turns) {
        return turns.stream()
                .map(turn -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Interviewer: ").append(turn.getAiQuestion());
                    if (turn.getUserAnswer() != null) {
                        sb.append("\nCandidate: ").append(turn.getUserAnswer());
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
    }
}
