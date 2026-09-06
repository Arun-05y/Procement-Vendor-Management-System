package com.procurea.procurementsystem.controller;

import com.procurea.procurementsystem.dto.AIAssistantRequest;
import com.procurea.procurementsystem.dto.AIAssistantResponse;
import com.procurea.procurementsystem.dto.ApiResponse;
import com.procurea.procurementsystem.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai-assistant")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AIAssistantResponse>> askQuestion(@RequestBody AIAssistantRequest request) {
        AIAssistantResponse response = aiAssistantService.askQuestion(request.getQuestion());
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }
}
