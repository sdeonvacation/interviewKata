package dev.interviewkata.service;

import dev.interviewkata.dto.DesignExerciseDto;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.repository.DesignExerciseRepository;
import dev.interviewkata.repository.DesignSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesignExerciseServiceTest {

    @Mock
    private DesignExerciseRepository designExerciseRepository;

    @Mock
    private DesignSubmissionRepository designSubmissionRepository;

    @InjectMocks
    private DesignExerciseService designExerciseService;

    private DesignExercise sampleExercise;

    @BeforeEach
    void setUp() {
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("System Design")
                .area(TopicArea.SYSTEM_DESIGN)
                .build();

        sampleExercise = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("Design a URL Shortener")
                .difficulty(Difficulty.MEDIUM)
                .build();
    }

    @Test
    void listExercises_nullDifficulty_returnsAll() {
        Page<DesignExercise> page = new PageImpl<>(List.of(sampleExercise));
        when(designExerciseRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<DesignExerciseDto> result = designExerciseService.listExercises(null, 0);

        assertEquals(1, result.getTotalElements());
        verify(designExerciseRepository).findAll(any(PageRequest.class));
        verify(designExerciseRepository, never()).findByDifficulty(any(), any());
    }

    @Test
    void listExercises_withDifficulty_filtersByDifficulty() {
        Page<DesignExercise> page = new PageImpl<>(List.of(sampleExercise));
        when(designExerciseRepository.findByDifficulty(eq(Difficulty.MEDIUM), any(PageRequest.class)))
                .thenReturn(page);

        Page<DesignExerciseDto> result = designExerciseService.listExercises(Difficulty.MEDIUM, 0);

        assertEquals(1, result.getTotalElements());
        verify(designExerciseRepository).findByDifficulty(eq(Difficulty.MEDIUM), any(PageRequest.class));
    }

    @Test
    void listExercises_emptyResult_returnsEmptyPage() {
        Page<DesignExercise> page = new PageImpl<>(List.of());
        when(designExerciseRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<DesignExerciseDto> result = designExerciseService.listExercises(null, 0);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
