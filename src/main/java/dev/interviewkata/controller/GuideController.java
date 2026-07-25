package dev.interviewkata.controller;

import dev.interviewkata.dto.GuideDto;
import dev.interviewkata.service.GuideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideDto> getGuideById(@PathVariable UUID id) {
        return ResponseEntity.ok(guideService.getGuideById(id));
    }
}
