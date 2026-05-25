package com.gridpulse.repository;

import com.gridpulse.model.PredictionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PredictionRecordRepository extends JpaRepository<PredictionRecord, Long> {
    @Query("""
            select count(p)
            from PredictionRecord p
            where p.predictedFailure = true and p.confirmedFailure = true
            """)
    long countTruePositives();

    @Query("""
            select count(p)
            from PredictionRecord p
            where p.predictedFailure = false and p.confirmedFailure = false
            """)
    long countTrueNegatives();

    @Query("""
            select count(p)
            from PredictionRecord p
            where p.predictedFailure = true and p.confirmedFailure = false
            """)
    long countFalsePositives();

    @Query("""
            select count(p)
            from PredictionRecord p
            where p.predictedFailure = false and p.confirmedFailure = true
            """)
    long countFalseNegatives();
}
