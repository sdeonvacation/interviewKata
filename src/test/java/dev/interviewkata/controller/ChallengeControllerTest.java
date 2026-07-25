package dev.interviewkata.controller;

import dev.interviewkata.dto.SubmitCodeRequest;
import dev.interviewkata.dto.SubmissionResultDto;
import dev.interviewkata.model.enums.SubmissionStatus;
import dev.interviewkata.service.ChallengeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerTest {

    @Mock
    private ChallengeService challengeService;

    private ChallengeController controller;

    @BeforeEach
    void setUp() {
        controller = new ChallengeController(challengeService);
    }

    @Test
    void submitSolution_validCode_returnsResult() {
        UUID id = UUID.randomUUID();
        SubmitCodeRequest request = new SubmitCodeRequest("System.out.println(\"hello\");");
        SubmissionResultDto dto = new SubmissionResultDto(
                UUID.randomUUID(), SubmissionStatus.PASSED, List.of(), "Good job", 100);
        when(challengeService.submitSolution(id, "System.out.println(\"hello\");")).thenReturn(dto);

        ResponseEntity<SubmissionResultDto> result = controller.submitSolution(id, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(dto, result.getBody());
        verify(challengeService).submitSolution(id, "System.out.println(\"hello\");");
    }

    @Test
    void runCode_validCode_returnsOutput() {
        SubmitCodeRequest request = new SubmitCodeRequest("int x = 42;");

        ResponseEntity<Map<String, String>> result = controller.runCode(request);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().get("output").contains("11 chars"));
    }

    @Test
    void runCode_codeWithVariousLength_reflectsCharCount() {
        SubmitCodeRequest request = new SubmitCodeRequest("abc");

        ResponseEntity<Map<String, String>> result = controller.runCode(request);

        assertTrue(result.getBody().get("output").contains("3 chars"));
    }
}
