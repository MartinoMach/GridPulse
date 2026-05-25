package com.gridpulse.controller;

import com.gridpulse.dto.AccuracyResponse;
import com.gridpulse.dto.AlertResponse;
import com.gridpulse.dto.PredictionResponse;
import com.gridpulse.service.PredictionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PredictionController {
    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/assets/{assetId}/prediction")
    public PredictionResponse prediction(@PathVariable String assetId) {
        return predictionService.predict(assetId);
    }

    @GetMapping("/alerts")
    public List<AlertResponse> alerts() {
        return predictionService.highRiskAlerts();
    }

    @GetMapping("/accuracy")
    public AccuracyResponse accuracy() {
        return predictionService.accuracy();
    }
}
