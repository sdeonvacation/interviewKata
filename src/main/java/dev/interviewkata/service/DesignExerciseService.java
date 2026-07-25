package dev.interviewkata.service;

import dev.interviewkata.dto.DesignExerciseDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.DesignSubmission;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.repository.DesignExerciseRepository;
import dev.interviewkata.repository.DesignSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DesignExerciseService {

    private final DesignExerciseRepository designExerciseRepository;
    private final DesignSubmissionRepository designSubmissionRepository;

    public DesignExerciseService(DesignExerciseRepository designExerciseRepository,
                                 DesignSubmissionRepository designSubmissionRepository) {
        this.designExerciseRepository = designExerciseRepository;
        this.designSubmissionRepository = designSubmissionRepository;
    }

    public Page<DesignExerciseDto> listExercises(Difficulty difficulty, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<DesignExercise> exercises;
        if (difficulty != null) {
            exercises = designExerciseRepository.findByDifficulty(difficulty, pageRequest);
        } else {
            exercises = designExerciseRepository.findAll(pageRequest);
        }
        return exercises.map(DtoMapper::toDto);
    }

    public DesignExercise getExerciseById(UUID id) {
        return designExerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Design exercise not found: " + id));
    }

    @Transactional
    public DesignSubmission submitAnswer(UUID exerciseId, String answer) {
        DesignExercise exercise = designExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new EntityNotFoundException("Design exercise not found: " + exerciseId));

        // Placeholder: persist submission, AI evaluation in future phase
        DesignSubmission submission = DesignSubmission.builder()
                .exercise(exercise)
                .answer(answer)
                .build();

        return designSubmissionRepository.save(submission);
    }
}
