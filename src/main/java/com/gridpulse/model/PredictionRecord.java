package com.gridpulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "prediction_records")
public class PredictionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "predicted_at", nullable = false)
    private Instant predictedAt;

    @Column(name = "failure_probability", nullable = false)
    private double failureProbability;

    @Column(name = "predicted_failure", nullable = false)
    private boolean predictedFailure;

    @Column(name = "confirmed_failure", nullable = false)
    private boolean confirmedFailure;

    @Column(name = "kdd_label", nullable = false)
    private String kddLabel;

    public Long getId() {
        return id;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Instant getPredictedAt() {
        return predictedAt;
    }

    public void setPredictedAt(Instant predictedAt) {
        this.predictedAt = predictedAt;
    }

    public double getFailureProbability() {
        return failureProbability;
    }

    public void setFailureProbability(double failureProbability) {
        this.failureProbability = failureProbability;
    }

    public boolean isPredictedFailure() {
        return predictedFailure;
    }

    public void setPredictedFailure(boolean predictedFailure) {
        this.predictedFailure = predictedFailure;
    }

    public boolean isConfirmedFailure() {
        return confirmedFailure;
    }

    public void setConfirmedFailure(boolean confirmedFailure) {
        this.confirmedFailure = confirmedFailure;
    }

    public String getKddLabel() {
        return kddLabel;
    }

    public void setKddLabel(String kddLabel) {
        this.kddLabel = kddLabel;
    }
}
