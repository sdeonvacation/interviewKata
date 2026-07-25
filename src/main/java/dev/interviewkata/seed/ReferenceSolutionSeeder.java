package dev.interviewkata.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.repository.ChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Seeds reference solutions into existing challenges by matching on title.
 * Only updates challenges that don't already have a reference_solution set.
 */
@Component
public class ReferenceSolutionSeeder {

    private static final Logger log = LoggerFactory.getLogger(ReferenceSolutionSeeder.class);

    private final ChallengeRepository challengeRepository;
    private final ObjectMapper yamlMapper;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public ReferenceSolutionSeeder(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.resourceResolver = new PathMatchingResourcePatternResolver();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedSolutions() {
        try {
            Resource[] resources = resourceResolver.getResources("classpath:seed/solutions/*.yaml");
            int updated = 0;
            for (Resource resource : resources) {
                updated += processFile(resource);
            }
            if (updated > 0) {
                log.info("Seeded {} reference solutions", updated);
            }
        } catch (IOException e) {
            log.error("Failed to scan solution seed files", e);
        }
    }

    private int processFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            SolutionSeedFile seedFile = yamlMapper.readValue(is, SolutionSeedFile.class);
            int count = 0;
            for (SolutionEntry entry : seedFile.solutions) {
                Optional<Challenge> challenge = challengeRepository.findByTitle(entry.title);
                if (challenge.isPresent()) {
                    Challenge c = challenge.get();
                    if (c.getReferenceSolution() == null || c.getReferenceSolution().isBlank()) {
                        c.setReferenceSolution(entry.referenceSolution);
                        challengeRepository.save(c);
                        count++;
                    }
                } else {
                    log.debug("No challenge found with title '{}' for solution seeding", entry.title);
                }
            }
            return count;
        } catch (Exception e) {
            log.error("Failed to process solution file: {}", resource.getFilename(), e);
            return 0;
        }
    }

    // YAML deserialization DTOs

    public static class SolutionSeedFile {
        public List<SolutionEntry> solutions;
    }

    public static class SolutionEntry {
        public String title;

        @JsonProperty("reference_solution")
        public String referenceSolution;
    }
}
