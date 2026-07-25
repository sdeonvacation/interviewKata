package dev.interviewkata.controller;

import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.dto.CardDto;
import dev.interviewkata.dto.GuideDto;
import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.service.CardService;
import dev.interviewkata.service.ChallengeService;
import dev.interviewkata.service.GuideService;
import dev.interviewkata.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final CardService cardService;
    private final GuideService guideService;
    private final ChallengeService challengeService;

    public TopicController(TopicService topicService,
                           CardService cardService,
                           GuideService guideService,
                           ChallengeService challengeService) {
        this.topicService = topicService;
        this.cardService = cardService;
        this.guideService = guideService;
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<List<TopicDto>> getTopicTree() {
        return ResponseEntity.ok(topicService.getTopicTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicDto> getTopicById(@PathVariable UUID id) {
        return ResponseEntity.ok(topicService.getTopicById(id));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<TopicDto>> getChildren(@PathVariable UUID id) {
        return ResponseEntity.ok(topicService.getChildren(id));
    }

    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardDto>> getCardsByTopic(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.getCardsByTopic(id));
    }

    @GetMapping("/{id}/guides")
    public ResponseEntity<List<GuideDto>> getGuidesByTopic(@PathVariable UUID id) {
        return ResponseEntity.ok(guideService.getGuidesByTopic(id));
    }

    @GetMapping("/{id}/challenges")
    public ResponseEntity<List<ChallengeDto>> getChallengesByTopic(@PathVariable UUID id) {
        return ResponseEntity.ok(challengeService.listByTopic(id));
    }

    @PostMapping("/{id}/generate-cards")
    public ResponseEntity<List<CardDto>> generateCards(@PathVariable UUID id) {
        List<CardDto> generated = cardService.generateCardsForTopic(id);
        return ResponseEntity.ok(generated);
    }
}
