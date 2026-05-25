package com.gridpulse.controller;

import com.gridpulse.model.NetworkEvent;
import com.gridpulse.service.PredictionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final PredictionService predictionService;

    public EventController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NetworkEvent ingest(@RequestBody NetworkEvent event) {
        return predictionService.ingest(event);
    }
}
