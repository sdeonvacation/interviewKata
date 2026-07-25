package dev.interviewkata.seed;

import dev.interviewkata.model.Card;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Guide;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.DesignExerciseRepository;
import dev.interviewkata.repository.GuideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class ContentSeeder {

    private static final Logger log = LoggerFactory.getLogger(ContentSeeder.class);

    private final YamlCardParser cardParser;
    private final YamlGuideParser guideParser;
    private final YamlChallengeParser challengeParser;
    private final YamlExerciseParser exerciseParser;
    private final CardRepository cardRepository;
    private final GuideRepository guideRepository;
    private final ChallengeRepository challengeRepository;
    private final DesignExerciseRepository exerciseRepository;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public ContentSeeder(
            YamlCardParser cardParser,
            YamlGuideParser guideParser,
            YamlChallengeParser challengeParser,
            YamlExerciseParser exerciseParser,
            CardRepository cardRepository,
            GuideRepository guideRepository,
            ChallengeRepository challengeRepository,
            DesignExerciseRepository exerciseRepository) {
        this.cardParser = cardParser;
        this.guideParser = guideParser;
        this.challengeParser = challengeParser;
        this.exerciseParser = exerciseParser;
        this.cardRepository = cardRepository;
        this.guideRepository = guideRepository;
        this.challengeRepository = challengeRepository;
        this.exerciseRepository = exerciseRepository;
        this.resourceResolver = new PathMatchingResourcePatternResolver();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        log.info("Content seeding started");

        int cardsSeeded = seedCards();
        int challengesSeeded = seedChallenges();
        int guidesSeeded = seedGuides();
        int exercisesSeeded = seedExercises();

        log.info("Seeded {} cards, {} challenges, {} guides, {} exercises",
                cardsSeeded, challengesSeeded, guidesSeeded, exercisesSeeded);
    }

    private int seedCards() {
        int count = 0;
        try {
            Resource[] resources = resourceResolver.getResources("classpath:seed/cards/*.yaml");
            for (Resource resource : resources) {
                count += processCardFile(resource);
            }
        } catch (IOException e) {
            log.error("Failed to scan card seed files", e);
        }
        return count;
    }

    private int seedChallenges() {
        int count = 0;
        try {
            Resource[] resources = resourceResolver.getResources("classpath:seed/challenges/*.yaml");
            for (Resource resource : resources) {
                count += processChallengeFile(resource);
            }
        } catch (IOException e) {
            log.error("Failed to scan challenge seed files", e);
        }
        return count;
    }

    private int seedGuides() {
        int count = 0;
        try {
            Resource[] resources = resourceResolver.getResources("classpath:seed/guides/*.yaml");
            for (Resource resource : resources) {
                count += processGuideFile(resource);
            }
        } catch (IOException e) {
            log.error("Failed to scan guide seed files", e);
        }
        return count;
    }

    private int processCardFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            List<Card> cards = cardParser.parse(is);
            int saved = 0;
            for (Card card : cards) {
                if (!cardExists(card)) {
                    cardRepository.save(card);
                    saved++;
                }
            }
            log.debug("Processed {}: {} new cards", resource.getFilename(), saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to process card file: {}", resource.getFilename(), e);
            return 0;
        }
    }

    private int processChallengeFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            List<Challenge> challenges = challengeParser.parse(is);
            int saved = 0;
            for (Challenge challenge : challenges) {
                if (!challengeExists(challenge)) {
                    challengeRepository.save(challenge);
                    saved++;
                }
            }
            log.debug("Processed {}: {} new challenges", resource.getFilename(), saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to process challenge file: {}", resource.getFilename(), e);
            return 0;
        }
    }

    private int processGuideFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            List<Guide> guides = guideParser.parse(is);
            int saved = 0;
            for (Guide guide : guides) {
                if (!guideExists(guide)) {
                    guideRepository.save(guide);
                    saved++;
                }
            }
            log.debug("Processed {}: {} new guides", resource.getFilename(), saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to process guide file: {}", resource.getFilename(), e);
            return 0;
        }
    }

    /**
     * Check if a card with the same front text and topic already exists.
     */
    private boolean cardExists(Card card) {
        UUID topicId = card.getTopic().getId();
        List<Card> existing = cardRepository.findByTopicId(topicId);
        return existing.stream()
                .anyMatch(c -> c.getFront().equals(card.getFront()));
    }

    /**
     * Check if a challenge with the same title and topic already exists.
     */
    private boolean challengeExists(Challenge challenge) {
        UUID topicId = challenge.getTopic().getId();
        List<Challenge> existing = challengeRepository.findByTopicId(topicId);
        return existing.stream()
                .anyMatch(c -> c.getTitle().equals(challenge.getTitle()));
    }

    /**
     * Check if a guide with the same title and topic already exists.
     */
    private boolean guideExists(Guide guide) {
        UUID topicId = guide.getTopic().getId();
        List<Guide> existing = guideRepository.findByTopicIdOrderBySortOrder(topicId);
        return existing.stream()
                .anyMatch(g -> g.getTitle().equals(guide.getTitle()));
    }

    private int seedExercises() {
        int count = 0;
        try {
            Resource[] resources = resourceResolver.getResources("classpath:seed/exercises/*.yaml");
            for (Resource resource : resources) {
                count += processExerciseFile(resource);
            }
        } catch (IOException e) {
            log.error("Failed to scan exercise seed files", e);
        }
        return count;
    }

    private int processExerciseFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            List<DesignExercise> exercises = exerciseParser.parse(is);
            int saved = 0;
            for (DesignExercise exercise : exercises) {
                if (!exerciseExists(exercise)) {
                    exerciseRepository.save(exercise);
                    saved++;
                }
            }
            log.debug("Processed {}: {} new exercises", resource.getFilename(), saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to process exercise file: {}", resource.getFilename(), e);
            return 0;
        }
    }

    /**
     * Check if an exercise with the same title and topic already exists.
     */
    private boolean exerciseExists(DesignExercise exercise) {
        UUID topicId = exercise.getTopic().getId();
        List<DesignExercise> existing = exerciseRepository.findByTopicId(topicId);
        return existing.stream()
                .anyMatch(e -> e.getTitle().equals(exercise.getTitle()));
    }
}
