package dev.interviewkata.service;

import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.GuideDto;
import dev.interviewkata.model.Guide;
import dev.interviewkata.repository.GuideRepository;
import dev.interviewkata.repository.QuizQuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GuideService {

    private final GuideRepository guideRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public GuideService(GuideRepository guideRepository, QuizQuestionRepository quizQuestionRepository) {
        this.guideRepository = guideRepository;
        this.quizQuestionRepository = quizQuestionRepository;
    }

    public List<GuideDto> getGuidesByTopic(UUID topicId) {
        return guideRepository.findByTopicIdOrderBySortOrder(topicId).stream()
                .map(guide -> DtoMapper.toDto(guide, getQuestionCount(guide.getId())))
                .toList();
    }

    public GuideDto getGuideById(UUID id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guide not found: " + id));
        return DtoMapper.toDto(guide, getQuestionCount(id));
    }

    private int getQuestionCount(UUID guideId) {
        return quizQuestionRepository.findByGuideId(guideId).size();
    }
}
