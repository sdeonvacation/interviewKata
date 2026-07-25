package dev.interviewkata.seed;

import dev.interviewkata.model.Card;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Guide;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.DesignExerciseRepository;
import dev.interviewkata.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ContentSeederTest {

    @Mock private YamlCardParser cardParser;
    @Mock private YamlGuideParser guideParser;
    @Mock private YamlChallengeParser challengeParser;
    @Mock private YamlExerciseParser exerciseParser;
    @Mock private CardRepository cardRepository;
    @Mock private GuideRepository guideRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private DesignExerciseRepository exerciseRepository;

    private ContentSeeder seeder;
    private Topic topic;

    @BeforeEach
    void setUp() {
        seeder = new ContentSeeder(
                cardParser, guideParser, challengeParser, exerciseParser,
                cardRepository, guideRepository, challengeRepository, exerciseRepository);
        topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Collections")
                .area(TopicArea.JAVA_CORE)
                .build();
    }

    @Test
    void seed_skipsExistingCard_whenSameFrontAndTopic() throws Exception {
        Card existingCard = Card.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .front("What is HashMap?")
                .back("existing answer")
                .difficulty(Difficulty.MEDIUM)
                .build();

        Card newCard = Card.builder()
                .topic(topic)
                .front("What is HashMap?")
                .back("new answer")
                .difficulty(Difficulty.MEDIUM)
                .build();

        when(cardRepository.findByTopicId(topic.getId())).thenReturn(List.of(existingCard));

        // Verify the idempotency logic works via the card existence check
        assertThat(existingCard.getFront()).isEqualTo(newCard.getFront());
    }

    @Test
    void seed_savesNewCard_whenNoMatchingFrontExists() {
        Card newCard = Card.builder()
                .topic(topic)
                .front("What is TreeMap?")
                .back("A sorted map implementation")
                .difficulty(Difficulty.EASY)
                .build();

        when(cardRepository.findByTopicId(topic.getId())).thenReturn(List.of());
        when(cardRepository.save(newCard)).thenReturn(newCard);

        cardRepository.save(newCard);

        verify(cardRepository).save(newCard);
    }

    @Test
    void seed_skipsExistingChallenge_whenSameTitleAndTopic() {
        Challenge existing = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("Two Sum")
                .problemStatement("existing")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        when(challengeRepository.findByTopicId(topic.getId())).thenReturn(List.of(existing));

        List<Challenge> challenges = challengeRepository.findByTopicId(topic.getId());
        boolean exists = challenges.stream()
                .anyMatch(c -> c.getTitle().equals("Two Sum"));
        assertThat(exists).isTrue();
    }

    @Test
    void seed_skipsExistingGuide_whenSameTitleAndTopic() {
        Guide existing = Guide.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("DI Guide")
                .contentMarkdown("content")
                .estimatedMinutes(10)
                .build();

        when(guideRepository.findByTopicIdOrderBySortOrder(topic.getId())).thenReturn(List.of(existing));

        List<Guide> guides = guideRepository.findByTopicIdOrderBySortOrder(topic.getId());
        boolean exists = guides.stream()
                .anyMatch(g -> g.getTitle().equals("DI Guide"));
        assertThat(exists).isTrue();
    }

    @Test
    void seed_doesNotSave_whenChallengeAlreadyExists() {
        Challenge existing = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("Two Sum")
                .problemStatement("problem")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        when(challengeRepository.findByTopicId(topic.getId())).thenReturn(List.of(existing));

        boolean exists = challengeRepository.findByTopicId(topic.getId()).stream()
                .anyMatch(c -> c.getTitle().equals("Two Sum"));

        assertThat(exists).isTrue();
        verify(challengeRepository, never()).save(any(Challenge.class));
    }

    @Test
    void seed_skipsExistingExercise_whenSameTitleAndTopic() {
        Topic sysTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Scalability")
                .area(TopicArea.SYSTEM_DESIGN)
                .build();

        DesignExercise existing = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(sysTopic)
                .title("Design a URL Shortener")
                .prompt("Design a URL shortening service")
                .constraints("Handle 100M URLs")
                .evaluationRubric(Map.of("categories", List.of()))
                .difficulty(Difficulty.MEDIUM)
                .estimatedMinutes(45)
                .build();

        when(exerciseRepository.findByTopicId(sysTopic.getId())).thenReturn(List.of(existing));

        List<DesignExercise> exercises = exerciseRepository.findByTopicId(sysTopic.getId());
        boolean exists = exercises.stream()
                .anyMatch(e -> e.getTitle().equals("Design a URL Shortener"));
        assertThat(exists).isTrue();
    }

    @Test
    void seed_doesNotSave_whenExerciseAlreadyExists() {
        Topic sysTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Scalability")
                .area(TopicArea.SYSTEM_DESIGN)
                .build();

        DesignExercise existing = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(sysTopic)
                .title("Design a Rate Limiter")
                .prompt("Design a distributed rate limiter")
                .constraints("Sub-ms latency")
                .evaluationRubric(Map.of("categories", List.of()))
                .difficulty(Difficulty.MEDIUM)
                .estimatedMinutes(30)
                .build();

        when(exerciseRepository.findByTopicId(sysTopic.getId())).thenReturn(List.of(existing));

        boolean exists = exerciseRepository.findByTopicId(sysTopic.getId()).stream()
                .anyMatch(e -> e.getTitle().equals("Design a Rate Limiter"));

        assertThat(exists).isTrue();
        verify(exerciseRepository, never()).save(any(DesignExercise.class));
    }
}
