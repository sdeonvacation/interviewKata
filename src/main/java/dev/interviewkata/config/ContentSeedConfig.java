package dev.interviewkata.config;

import dev.interviewkata.seed.ContentSeeder;
import dev.interviewkata.seed.TopicResolver;
import dev.interviewkata.seed.YamlCardParser;
import dev.interviewkata.seed.YamlChallengeParser;
import dev.interviewkata.seed.YamlExerciseParser;
import dev.interviewkata.seed.YamlGuideParser;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.DesignExerciseRepository;
import dev.interviewkata.repository.GuideRepository;
import dev.interviewkata.repository.TopicRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "interviewkata.seed.enabled", havingValue = "true")
public class ContentSeedConfig {

    @Bean
    public TopicResolver topicResolver(TopicRepository topicRepository) {
        return new TopicResolver(topicRepository);
    }

    @Bean
    public YamlCardParser yamlCardParser(TopicResolver topicResolver) {
        return new YamlCardParser(topicResolver);
    }

    @Bean
    public YamlGuideParser yamlGuideParser(TopicResolver topicResolver) {
        return new YamlGuideParser(topicResolver);
    }

    @Bean
    public YamlChallengeParser yamlChallengeParser(TopicResolver topicResolver) {
        return new YamlChallengeParser(topicResolver);
    }

    @Bean
    public YamlExerciseParser yamlExerciseParser(TopicResolver topicResolver) {
        return new YamlExerciseParser(topicResolver);
    }

    @Bean
    public ContentSeeder contentSeeder(
            YamlCardParser cardParser,
            YamlGuideParser guideParser,
            YamlChallengeParser challengeParser,
            YamlExerciseParser exerciseParser,
            CardRepository cardRepository,
            GuideRepository guideRepository,
            ChallengeRepository challengeRepository,
            DesignExerciseRepository exerciseRepository) {
        return new ContentSeeder(
                cardParser, guideParser, challengeParser, exerciseParser,
                cardRepository, guideRepository, challengeRepository, exerciseRepository);
    }
}
