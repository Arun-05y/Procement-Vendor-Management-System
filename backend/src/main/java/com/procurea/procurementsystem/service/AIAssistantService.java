package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.AIAssistantResponse;

public interface AIAssistantService {
    AIAssistantResponse askQuestion(String question);
}
