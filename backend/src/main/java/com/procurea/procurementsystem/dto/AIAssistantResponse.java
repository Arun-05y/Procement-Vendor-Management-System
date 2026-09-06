package com.procurea.procurementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAssistantResponse {
    private String answer;
    private Object dataSource; // Shows the actual DB records used to generate the answer
}
