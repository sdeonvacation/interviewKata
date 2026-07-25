package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.ChallengeDetailDto;
import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.SubmissionResultDto;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Submission;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.SubmissionStatus;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.SubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final AiService aiService;

    public ChallengeService(ChallengeRepository challengeRepository,
                            SubmissionRepository submissionRepository,
                            AiService aiService) {
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.aiService = aiService;
    }

    public Page<ChallengeDto> listChallenges(ChallengeType type, Difficulty difficulty, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Challenge> challenges;
        if (type != null && difficulty != null) {
            challenges = challengeRepository.findByChallengeTypeAndDifficulty(type, difficulty, pageRequest);
        } else if (type != null) {
            challenges = challengeRepository.findByChallengeType(type, pageRequest);
        } else if (difficulty != null) {
            challenges = challengeRepository.findByDifficulty(difficulty, pageRequest);
        } else {
            challenges = challengeRepository.findAll(pageRequest);
        }
        return challenges.map(c -> DtoMapper.toDto(c, hasSolved(c.getId())));
    }

    public List<ChallengeDto> listByTopic(UUID topicId) {
        return challengeRepository.findByTopicId(topicId).stream()
                .map(c -> DtoMapper.toDto(c, hasSolved(c.getId())))
                .toList();
    }

    public ChallengeDetailDto getChallengeDetail(UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + id));

        List<SubmissionResultDto> submissions = submissionRepository
                .findByChallengeIdOrderBySubmittedAtDesc(id).stream()
                .map(DtoMapper::toDto)
                .toList();

        return DtoMapper.toDetailDto(challenge, submissions);
    }

    @Transactional
    public SubmissionResultDto submitSolution(UUID challengeId, String code) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + challengeId));

        // Placeholder: persist with PASSED status (real sandbox execution in Phase 4)
        Submission submission = Submission.builder()
                .challenge(challenge)
                .code(code)
                .status(SubmissionStatus.PASSED)
                .testResults(List.of())
                .build();

        // Generate AI code review (non-blocking failure)
        try {
            String aiReview = aiService.reviewCode(code, challenge.getProblemStatement());
            submission.setAiReview(aiReview);
        } catch (Exception e) {
            log.warn("Failed to generate AI code review for challenge {}: {}", challengeId, e.getMessage());
        }

        Submission saved = submissionRepository.save(submission);
        return DtoMapper.toDto(saved);
    }

    private boolean hasSolved(UUID challengeId) {
        return submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId).stream()
                .anyMatch(s -> s.getStatus() == SubmissionStatus.PASSED);
    }
}
