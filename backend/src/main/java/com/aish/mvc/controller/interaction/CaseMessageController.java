package com.aish.mvc.controller.interaction;

import com.aish.mvc.dto.interaction.CaseMessageDTO;
import com.aish.mvc.dto.interaction.CreateCaseMessageRequestDTO;
import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.service.interaction.CaseMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

// Thread trao đổi 2 chiều cho case REPORT/APPEAL (IH-5). Không phủ permitAll trong
// SecurityConfig -> yêu cầu đăng nhập theo mặc định (.anyRequest().authenticated()).
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseMessageController {

    private final CaseMessageService caseMessageService;

    @GetMapping("/{type}/{id}/messages")
    public ResponseEntity<List<CaseMessageDTO>> listMessages(
            @PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(caseMessageService.listMessages(parseCaseType(type), id));
    }

    @PostMapping("/{type}/{id}/messages")
    public ResponseEntity<CaseMessageDTO> postMessage(
            @PathVariable String type, @PathVariable Long id,
            @RequestBody CreateCaseMessageRequestDTO request) {
        CaseMessageDTO created = caseMessageService.postMessage(parseCaseType(type), id, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private CaseType parseCaseType(String type) {
        try {
            return CaseType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("error.caseMessage.invalidType");
        }
    }
}
