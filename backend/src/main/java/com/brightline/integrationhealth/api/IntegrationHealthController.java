package com.brightline.integrationhealth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class IntegrationHealthController {
    @GetMapping("/health/summary")
    public Map<String, String> summary() {
        return Map.of("status", "NOT_IMPLEMENTED");
    }
}
