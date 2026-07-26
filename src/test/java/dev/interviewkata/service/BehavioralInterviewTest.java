package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.InterviewPhase;
import dev.interviewkata.model.enums.InterviewState;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.InterviewTurnRepository;
import dev.interviewkata.repository.MockInterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BehavioralInterviewTest {

    @Mock
    private MockInterviewRepository mockInterviewRepository;

    @Mock
    private InterviewTurnRepository interviewTurnRepository;

    @Mock
    private AiService aiService;

    private MockInterviewEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MockInterviewEngine(mockInterviewRepository, interviewTurnRepository, aiService);
    }

    @Test
    void startInterview_behavioral_usesBehavioralPrompt() {
        when(mockInterviewRepository.countByStartedAtAfter(any())).thenReturn(0L);
        when(aiService.conductBehavioralInterview(eq(""), eq("BEHAVIORAL"), eq("INTRO")))
                .thenReturn("Welcome! Tell me briefly about your current role.");

        MockInterview interview = MockInterview.builder()
                .id(UUID.randomUUID())
                .topicArea(TopicArea.BEHAVIORAL)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        engine.startInterview(TopicArea.BEHAVIORAL, Difficulty.MEDIUM);

        verify(aiService).conductBehavioralInterview(eq(""), eq("BEHAVIORAL"), eq("INTRO"));
        verify(aiService, never()).conductInterview(any(), any(), any());

        ArgumentCaptor<InterviewTurn> turnCaptor = ArgumentCaptor.forClass(InterviewTurn.class);
        verify(interviewTurnRepository).save(turnCaptor.capture());
        assertEquals("Welcome! Tell me briefly about your current role.", turnCaptor.getValue().getAiQuestion());
        assertEquals(InterviewPhase.INTRO, turnCaptor.getValue().getPhase());
    }

    @Test
    void startInterview_technical_usesRegularPrompt() {
        when(mockInterviewRepository.countByStartedAtAfter(any())).thenReturn(0L);
        when(aiService.conductInterview(eq(""), eq("DSA"), eq("INTRO")))
                .thenReturn("What data structures are you most familiar with?");

        MockInterview interview = MockInterview.builder()
                .id(UUID.randomUUID())
                .topicArea(TopicArea.DSA)
                .difficulty(Difficulty.EASY)
                .state(InterviewState.ASKING)
                .build();
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        engine.startInterview(TopicArea.DSA, Difficulty.EASY);

        verify(aiService).conductInterview(eq(""), eq("DSA"), eq("INTRO"));
        verify(aiService, never()).conductBehavioralInterview(any(), any(), any());
    }

    @Test
    void submitAnswer_behavioral_usesBehavioralPhases() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.BEHAVIORAL)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();

        InterviewTurn turn1 = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(1)
                .aiQuestion("Tell me about your current role.")
                .phase(InterviewPhase.INTRO)
                .build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId))
                .thenReturn(List.of(turn1));
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(aiService.conductBehavioralInterview(any(), eq("BEHAVIORAL"), any()))
                .thenReturn("Tell me about a time you took initiative.");

        InterviewTurn result = engine.submitAnswer(interviewId, "I'm a senior backend dev...");

        verify(aiService).conductBehavioralInterview(any(), eq("BEHAVIORAL"), any());
        assertEquals("Tell me about a time you took initiative.", result.getAiQuestion());
    }

    @Test
    void submitAnswer_behavioral_progressesToQuestionPhase() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.BEHAVIORAL)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();

        // Simulate 2 existing turns — next (turn 3) should be QUESTION phase
        InterviewTurn turn1 = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(1)
                .aiQuestion("Welcome!")
                .userAnswer("Hi, I'm a dev")
                .phase(InterviewPhase.INTRO)
                .build();
        InterviewTurn turn2 = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(2)
                .aiQuestion("Tell me about a time you led a project.")
                .phase(InterviewPhase.QUESTION)
                .build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId))
                .thenReturn(List.of(turn1, turn2));
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(aiService.conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("QUESTION")))
                .thenReturn("Can you give me a specific example?");

        InterviewTurn result = engine.submitAnswer(interviewId, "I led a microservices migration...");

        // Turn 3 should be QUESTION phase (turns 2-3 are QUESTION)
        verify(aiService).conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("QUESTION"));
        assertEquals(InterviewPhase.QUESTION, result.getPhase());
    }

    @Test
    void submitAnswer_behavioral_progressesToProbePhase() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.BEHAVIORAL)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();

        // Simulate 3 existing turns — next (turn 4) should be PROBE phase
        InterviewTurn turn1 = InterviewTurn.builder().interview(interview).turnNumber(1)
                .aiQuestion("Welcome").userAnswer("Hi").phase(InterviewPhase.INTRO).build();
        InterviewTurn turn2 = InterviewTurn.builder().interview(interview).turnNumber(2)
                .aiQuestion("Q1").userAnswer("A1").phase(InterviewPhase.QUESTION).build();
        InterviewTurn turn3 = InterviewTurn.builder().interview(interview).turnNumber(3)
                .aiQuestion("Q2").phase(InterviewPhase.QUESTION).build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId))
                .thenReturn(List.of(turn1, turn2, turn3));
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(aiService.conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("PROBE")))
                .thenReturn("What was YOUR specific role in that?");

        InterviewTurn result = engine.submitAnswer(interviewId, "Our team worked together...");

        verify(aiService).conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("PROBE"));
        assertEquals(InterviewPhase.PROBE, result.getPhase());
    }

    @Test
    void submitAnswer_behavioral_reachesWrapUp() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.BEHAVIORAL)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();

        // Simulate 7 turns — next (turn 8) should be WRAP_UP
        List<InterviewTurn> turns = List.of(
                InterviewTurn.builder().interview(interview).turnNumber(1).aiQuestion("Q1").userAnswer("A1").phase(InterviewPhase.INTRO).build(),
                InterviewTurn.builder().interview(interview).turnNumber(2).aiQuestion("Q2").userAnswer("A2").phase(InterviewPhase.QUESTION).build(),
                InterviewTurn.builder().interview(interview).turnNumber(3).aiQuestion("Q3").userAnswer("A3").phase(InterviewPhase.QUESTION).build(),
                InterviewTurn.builder().interview(interview).turnNumber(4).aiQuestion("Q4").userAnswer("A4").phase(InterviewPhase.PROBE).build(),
                InterviewTurn.builder().interview(interview).turnNumber(5).aiQuestion("Q5").userAnswer("A5").phase(InterviewPhase.PROBE).build(),
                InterviewTurn.builder().interview(interview).turnNumber(6).aiQuestion("Q6").userAnswer("A6").phase(InterviewPhase.FOLLOW_UP).build(),
                InterviewTurn.builder().interview(interview).turnNumber(7).aiQuestion("Q7").phase(InterviewPhase.FOLLOW_UP).build()
        );

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId)).thenReturn(turns);
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(aiService.conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("WRAP_UP")))
                .thenReturn("Thank you for sharing. Focus on quantifying your results.");

        InterviewTurn result = engine.submitAnswer(interviewId, "The result was successful.");

        verify(aiService).conductBehavioralInterview(any(), eq("BEHAVIORAL"), eq("WRAP_UP"));
        assertEquals(InterviewPhase.WRAP_UP, result.getPhase());
    }
}
