package com.gridpulse.service;

import com.gridpulse.config.GridPulseProperties;
import com.gridpulse.dto.AccuracyResponse;
import com.gridpulse.dto.AlertResponse;
import com.gridpulse.dto.PredictionResponse;
import com.gridpulse.model.NetworkEvent;
import com.gridpulse.model.PredictionRecord;
import com.gridpulse.model.Severity;
import com.gridpulse.repository.NetworkEventRepository;
import com.gridpulse.repository.PredictionRecordRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionService {
    private final NetworkEventRepository eventRepository;
    private final PredictionRecordRepository predictionRepository;
    private final GridPulseProperties properties;

    public PredictionService(
            NetworkEventRepository eventRepository,
            PredictionRecordRepository predictionRepository,
            GridPulseProperties properties) {
        this.eventRepository = eventRepository;
        this.predictionRepository = predictionRepository;
        this.properties = properties;
    }

    @Transactional
    public NetworkEvent ingest(NetworkEvent event) {
        NetworkEvent saved = eventRepository.save(event);
        recordPredictionFor(saved.getAssetId(), saved.getKddLabel());
        return saved;
    }

    @Transactional(readOnly = true)
    public PredictionResponse predict(String assetId) {
        List<NetworkEvent> events = recentEvents(assetId);
        double probability = score(events);
        return new PredictionResponse(
                assetId,
                probability,
                estimateTimeToFailure(probability),
                riskBand(probability));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> highRiskAlerts() {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(properties.prediction().lookbackMinutes()));
        return eventRepository.findActiveAssetIds(windowStart).stream()
                .map(this::predict)
                .filter(prediction -> prediction.failureProbability() >= properties.prediction().highRiskThreshold())
                .sorted(Comparator.comparing(PredictionResponse::failureProbability).reversed())
                .map(prediction -> new AlertResponse(
                        prediction.assetId(),
                        prediction.failureProbability(),
                        prediction.estimatedTimeToFailure(),
                        prediction.riskBand()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AccuracyResponse accuracy() {
        long total = predictionRepository.count();
        long falsePositives = predictionRepository.countFalsePositives();
        long falseNegatives = predictionRepository.countFalseNegatives();
        long correct = predictionRepository.countTruePositives() + predictionRepository.countTrueNegatives();
        double accuracy = total == 0 ? 0.0 : (correct * 100.0) / total;
        return new AccuracyResponse(total, correct, falsePositives, falseNegatives, accuracy);
    }

    private void recordPredictionFor(String assetId, String kddLabel) {
        PredictionResponse prediction = predict(assetId);
        PredictionRecord record = new PredictionRecord();
        record.setAssetId(assetId);
        record.setPredictedAt(Instant.now());
        record.setFailureProbability(prediction.failureProbability());
        record.setPredictedFailure(prediction.failureProbability() >= properties.prediction().highRiskThreshold());
        record.setConfirmedFailure(isFailureLabel(kddLabel));
        record.setKddLabel(kddLabel);
        predictionRepository.save(record);
    }

    private List<NetworkEvent> recentEvents(String assetId) {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(properties.prediction().lookbackMinutes()));
        return eventRepository.findRecentEventsByAssetWithinWindow(assetId, windowStart);
    }

    private boolean isFailureLabel(String label) {
        String normalized = normalizeLabel(label);
        return !properties.prediction().positiveLabelsNormalizedValue().equals(normalized);
    }

    private double score(List<NetworkEvent> events) {
        if (events.isEmpty()) {
            return 0.0;
        }

        long critical = events.stream().filter(event -> event.getSeverity() == Severity.CRITICAL).count();
        long high = events.stream().filter(event -> event.getSeverity() == Severity.HIGH).count();
        long medium = events.stream().filter(event -> event.getSeverity() == Severity.MEDIUM).count();
        long confirmedFailures = events.stream().filter(event -> isFailureLabel(event.getKddLabel())).count();

        double severityPressure = Math.min(1.0, (critical * 1.0 + high * 0.65 + medium * 0.25) / 12.0);
        double failureEvidence = Math.min(1.0, confirmedFailures / Math.max(1.0, events.size() * 0.35));
        double volumePressure = Math.min(1.0, averageTrafficVolume(events) / 1_000_000.0);

        // Sliding-window heuristic favors leading indicators over labels; labels are mostly used for scoring.
        return clamp((severityPressure * 0.55) + (volumePressure * 0.25) + (failureEvidence * 0.20));
    }

    private double averageTrafficVolume(List<NetworkEvent> events) {
        return events.stream()
                .map(NetworkEvent::getMetadata)
                .mapToDouble(metadata -> ((Number) metadata.getOrDefault("trafficVolume", 0)).doubleValue())
                .average()
                .orElse(0.0);
    }

    private Instant estimateTimeToFailure(double probability) {
        long minutes = Math.max(1, Math.round(30 - (probability * 29)));
        return Instant.now().plus(Duration.ofMinutes(minutes));
    }

    private String riskBand(double probability) {
        if (probability >= 0.9) {
            return "critical";
        }
        if (probability >= properties.prediction().highRiskThreshold()) {
            return "warning";
        }
        return "healthy";
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.toLowerCase().replace(".", "").trim();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
