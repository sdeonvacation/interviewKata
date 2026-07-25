package dev.interviewkata.controller;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.CardDto;
import dev.interviewkata.model.Card;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final AiService aiService;
    private final CardRepository cardRepository;

    public CardController(CardService cardService, AiService aiService, CardRepository cardRepository) {
        this.cardService = cardService;
        this.aiService = aiService;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/due")
    public ResponseEntity<Map<String, Long>> getDueCardCount() {
        long count = cardService.getDueCardCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @PostMapping("/{id}/explain")
    public ResponseEntity<Map<String, String>> generateExplanation(@PathVariable UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + id));

        String explanation = aiService.generateExplanation(card.getFront(), card.getBack());
        card.setExplanation(explanation);
        cardRepository.save(card);

        return ResponseEntity.ok(Map.of("explanation", explanation));
    }
}
