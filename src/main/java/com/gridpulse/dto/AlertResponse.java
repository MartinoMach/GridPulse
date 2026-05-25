package com.gridpulse.dto;

import java.time.Instant;

public record AlertResponse(
        String assetId,
        double failureProbability,
        Instant estimatedTimeToFailure,
        String severity
) {
}
