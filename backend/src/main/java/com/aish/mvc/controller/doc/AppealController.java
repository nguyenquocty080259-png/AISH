package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;
import com.aish.mvc.service.doc.ModerationAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final ModerationAppealService moderationAppealService;

    @GetMapping("/mine")
    public ResponseEntity<List<ModerationAppealResponseDTO>> getMyAppeals() {
        return ResponseEntity.ok(moderationAppealService.listMyAppeals());
    }
}
