package dev.interviewkata.controller;

import dev.interviewkata.dto.ReviewSessionDto;
import dev.interviewkata.dto.StartReviewRequest;
import dev.interviewkata.service.ReviewSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewSessionService reviewSessionService;

    private ReviewController controller;

    @BeforeEach
    void setUp() {
        controller = new ReviewController(reviewSessionService);
    }

    @Test
    void startSession_withTopicAndLimit_passesValues() {
        UUID topicId = UUID.randomUUID();
        StartReviewRequest request = new StartReviewRequest(topicId, 10);
        ReviewSessionDto dto = new ReviewSessionDto(UUID.randomUUID(), List.of(), 0);
        when(reviewSessionService.startSession(topicId, 10)).thenReturn(dto);

        ResponseEntity<ReviewSessionDto> result = controller.startSession(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
        verify(reviewSessionService).startSession(topicId, 10);
    }

    @Test
    void startSession_nullLimit_defaults20() {
        StartReviewRequest request = new StartReviewRequest(null, null);
        ReviewSessionDto dto = new ReviewSessionDto(UUID.randomUUID(), List.of(), 0);
        when(reviewSessionService.startSession(null, 20)).thenReturn(dto);

        ResponseEntity<ReviewSessionDto> result = controller.startSession(request);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewSessionService).startSession(null, 20);
    }

    @Test
    void startSession_nullTopicId_passesNull() {
        UUID topicId = null;
        StartReviewRequest request = new StartReviewRequest(topicId, 5);
        ReviewSessionDto dto = new ReviewSessionDto(UUID.randomUUID(), List.of(), 0);
        when(reviewSessionService.startSession(null, 5)).thenReturn(dto);

        ResponseEntity<ReviewSessionDto> result = controller.startSession(request);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewSessionService).startSession(null, 5);
    }
}
