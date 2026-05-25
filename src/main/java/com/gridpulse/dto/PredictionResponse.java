package com.gridpulse.dto;

import java.time.Instant;

public record PredictionResponse(
        String assetId,
        double failureProbability,
        Instant estimatedTimeToFailure,
        String riskBand
) {
}
