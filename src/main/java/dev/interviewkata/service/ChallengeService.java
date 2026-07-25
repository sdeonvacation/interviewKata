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
import dev.interviewkata.sandbox.JShellSandbox;
import dev.interviewkata.sandbox.TestCase;
import dev.interviewkata.sandbox.TestCaseResult;
import dev.interviewkata.sandbox.TestResult;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final AiService aiService;
    private final ChallengePracticeService practiceSer;
    private final JShellSandbox jshellSandbox;

    public ChallengeService(ChallengeRepository challengeRepository,
                            SubmissionRepository submissionRepository,
                            AiService aiService,
                            ChallengePracticeService practiceSer,
                            JShellSandbox jshellSandbox) {
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.aiService = aiService;
        this.practiceSer = practiceSer;
        this.jshellSandbox = jshellSandbox;
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

        boolean solved = hasSolved(id);
        return DtoMapper.toDetailDto(challenge, submissions, solved);
    }

    @Transactional
    public SubmissionResultDto submitSolution(UUID challengeId, String code, boolean withAiReview) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + challengeId));

        // Execute code against test cases in JShell sandbox
        List<TestCase> testCases = mapTestCases(challenge.getTestCases());
        String starterCode = challenge.getStarterCode();
        TestResult testResult = jshellSandbox.executeWithTests(code, starterCode, testCases);

        // Determine submission status
        SubmissionStatus status = determineStatus(testResult);

        // Build test results for persistence
        List<Map<String, Object>> testResultMaps = mapTestResultsToJson(testResult);

        Submission submission = Submission.builder()
                .challenge(challenge)
                .code(code)
                .status(status)
                .testResults(testResultMaps)
                .executionTimeMs((int) testResult.totalDurationMs())
                .build();

        // Generate AI code review only if requested
        if (withAiReview) {
            try {
                String aiReview = aiService.reviewCode(code, challenge.getProblemStatement());
                submission.setAiReview(aiReview);
            } catch (Exception e) {
                log.warn("Failed to generate AI code review for challenge {}: {}", challengeId, e.getMessage());
            }
        }

        Submission saved = submissionRepository.save(submission);

        // Schedule for spaced repetition re-practice
        practiceSer.scheduleNextPractice(challengeId);

        return DtoMapper.toDto(saved);
    }

    private List<TestCase> mapTestCases(List<Map<String, Object>> rawTestCases) {
        if (rawTestCases == null || rawTestCases.isEmpty()) {
            return List.of();
        }
        return rawTestCases.stream()
                .map(tc -> new TestCase(
                        (String) tc.get("input"),
                        (String) tc.get("expected"),
                        (String) tc.getOrDefault("description", "Test case")
                ))
                .toList();
    }

    private SubmissionStatus determineStatus(TestResult testResult) {
        if (testResult.isTimeout()) {
            return SubmissionStatus.TIMEOUT;
        }
        if (testResult.hasCompilationError()) {
            return SubmissionStatus.ERROR;
        }
        if (testResult.allPassed()) {
            return SubmissionStatus.PASSED;
        }
        return SubmissionStatus.FAILED;
    }

    private List<Map<String, Object>> mapTestResultsToJson(TestResult testResult) {
        if (testResult.details() == null || testResult.details().isEmpty()) {
            if (testResult.hasCompilationError()) {
                return List.of(Map.of(
                        "description", "Compilation",
                        "passed", false,
                        "error", testResult.compilationError()
                ));
            }
            if (testResult.isTimeout()) {
                return List.of(Map.of(
                        "description", "Execution",
                        "passed", false,
                        "error", "Execution timed out"
                ));
            }
            return List.of();
        }
        return testResult.details().stream()
                .map(this::testCaseResultToMap)
                .toList();
    }

    private Map<String, Object> testCaseResultToMap(TestCaseResult tcr) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("description", tcr.description());
        map.put("passed", tcr.passed());
        if (tcr.actual() != null) {
            map.put("actual", tcr.actual());
        }
        map.put("expected", tcr.expected());
        if (tcr.error() != null) {
            map.put("error", tcr.error());
        }
        return map;
    }

    private boolean hasSolved(UUID challengeId) {
        return submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId).stream()
                .anyMatch(s -> s.getStatus() == SubmissionStatus.PASSED);
    }
}
