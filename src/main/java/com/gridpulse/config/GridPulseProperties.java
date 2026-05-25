package com.gridpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gridpulse")
public record GridPulseProperties(
        Kafka kafka,
        Replay replay,
        Simulator simulator,
        Prediction prediction
) {
    public record Kafka(String networkEventsTopic, int consumerConcurrency, int workerPoolSize) {
    }

    public record Replay(boolean enabled, String csvPath, int targetEventsPerSecond, int tickMillis) {
    }

    public record Simulator(boolean enabled, int targetEventsPerSecond) {
    }

    public record Prediction(int lookbackMinutes, double highRiskThreshold, String positiveLabelsNormalizedValue) {
    }
}
