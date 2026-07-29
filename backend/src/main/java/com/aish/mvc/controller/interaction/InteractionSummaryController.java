package com.aish.mvc.controller.interaction;

import com.aish.mvc.dto.interaction.InteractionSummaryDTO;
import com.aish.mvc.service.interaction.InteractionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionSummaryController {

    private final InteractionSummaryService interactionSummaryService;

    @GetMapping("/summary")
    public ResponseEntity<InteractionSummaryDTO> getSummary() {
        return ResponseEntity.ok(interactionSummaryService.getSummary());
    }
}
