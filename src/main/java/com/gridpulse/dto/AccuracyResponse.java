package com.gridpulse.dto;

public record AccuracyResponse(
        long totalPredictionsMade,
        long correctPredictions,
        long falsePositives,
        long falseNegatives,
        double overallAccuracyPercent
) {
}
