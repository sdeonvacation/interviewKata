package dev.interviewkata.seed;

import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.ChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceSolutionSeederTest {

    @Mock
    private ChallengeRepository challengeRepository;

    private ReferenceSolutionSeeder seeder;

    private Challenge challenge;

    @BeforeEach
    void setUp() {
        seeder = new ReferenceSolutionSeeder(challengeRepository);

        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("DSA")
                .area(TopicArea.DSA)
                .build();

        challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("Two Sum")
                .challengeType(ChallengeType.DSA)
                .difficulty(Difficulty.EASY)
                .problemStatement("Find two numbers")
                .build();
    }

    @Test
    void seedSolutions_matchesByTitle_updatesSolution() {
        when(challengeRepository.findByTitle("Two Sum")).thenReturn(java.util.List.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Trigger the seed — uses classpath resources
        seeder.seedSolutions();

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository, atLeastOnce()).save(captor.capture());

        Challenge saved = captor.getValue();
        assertNotNull(saved.getReferenceSolution());
        assertTrue(saved.getReferenceSolution().contains("HashMap"));
    }

    @Test
    void seedSolutions_challengeAlreadyHasSolution_doesNotOverwrite() {
        challenge.setReferenceSolution("Existing solution");
        when(challengeRepository.findByTitle("Two Sum")).thenReturn(java.util.List.of(challenge));

        seeder.seedSolutions();

        verify(challengeRepository, never()).save(any());
    }

    @Test
    void seedSolutions_titleNotFound_skipsSilently() {
        when(challengeRepository.findByTitle(any())).thenReturn(java.util.List.of());

        assertDoesNotThrow(() -> seeder.seedSolutions());
        verify(challengeRepository, never()).save(any());
    }
}
