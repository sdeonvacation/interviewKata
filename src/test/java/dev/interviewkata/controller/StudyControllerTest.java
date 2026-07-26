package dev.interviewkata.controller;

import dev.interviewkata.dto.StudyMessageDto;
import dev.interviewkata.dto.StudySessionDto;
import dev.interviewkata.dto.StudySessionSummaryDto;
import dev.interviewkata.service.StudySessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyControllerTest {

    @Mock
    private StudySessionService studySessionService;

    private StudyController controller;

    @BeforeEach
    void setUp() {
        controller = new StudyController(studySessionService);
    }

    @Test
    void createSession_delegatesToService() {
        UUID topicId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        StudySessionDto dto = new StudySessionDto(sessionId, topicId, "HashMap Internals", "JAVA_CORE",
                LocalDateTime.now(), LocalDateTime.now(), 0, List.of());
        when(studySessionService.createSession(topicId)).thenReturn(dto);

        var request = new StudyController.CreateSessionRequest(topicId);
        ResponseEntity<StudySessionDto> result = controller.createSession(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
        verify(studySessionService).createSession(topicId);
    }

    @Test
    void deleteSession_delegatesToService() {
        UUID sessionId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteSession(sessionId);

        assertEquals(204, result.getStatusCode().value());
        verify(studySessionService).deleteSession(sessionId);
    }

    @Test
    void sendMessage_delegatesToService() {
        UUID sessionId = UUID.randomUUID();
        StudyMessageDto dto = new StudyMessageDto("AI", "Great question!", 3);
        when(studySessionService.sendMessage(sessionId, "What about load factor?")).thenReturn(dto);

        var request = new StudyController.SendMessageRequest("What about load factor?");
        ResponseEntity<StudyMessageDto> result = controller.sendMessage(sessionId, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
        verify(studySessionService).sendMessage(sessionId, "What about load factor?");
    }

    @Test
    void listSessions_delegatesToService() {
        StudySessionSummaryDto summary = new StudySessionSummaryDto(UUID.randomUUID(), UUID.randomUUID(),
                "Spring DI", "SPRING_BOOT", LocalDateTime.now(), LocalDateTime.now(), 4, "Explain DI");
        when(studySessionService.listSessions()).thenReturn(List.of(summary));

        ResponseEntity<List<StudySessionSummaryDto>> result = controller.listSessions();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(studySessionService).listSessions();
    }

    @Test
    void getSession_delegatesToService() {
        UUID sessionId = UUID.randomUUID();
        StudySessionDto dto = new StudySessionDto(sessionId, UUID.randomUUID(), "Kafka", "ARCHITECTURE",
                LocalDateTime.now(), LocalDateTime.now(), 2,
                List.of(new StudyMessageDto("USER", "What is a partition?", 0)));
        when(studySessionService.getSession(sessionId)).thenReturn(dto);

        ResponseEntity<StudySessionDto> result = controller.getSession(sessionId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
        verify(studySessionService).getSession(sessionId);
    }
}
