package com.gridpulse.ingestion;

import com.gridpulse.model.NetworkEvent;
import com.gridpulse.service.PredictionService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class NetworkEventKafkaConsumer {
    private final ExecutorService ingestionWorkerPool;
    private final PredictionService predictionService;

    public NetworkEventKafkaConsumer(ExecutorService ingestionWorkerPool, PredictionService predictionService) {
        this.ingestionWorkerPool = ingestionWorkerPool;
        this.predictionService = predictionService;
    }

    @KafkaListener(
            topics = "${gridpulse.kafka.network-events-topic}",
            containerFactory = "networkEventKafkaListenerContainerFactory")
    public void ingest(ConsumerRecord<String, NetworkEvent> record, Acknowledgment acknowledgment) {
        CompletableFuture
                .runAsync(() -> predictionService.ingest(record.value()), ingestionWorkerPool)
                // Offsets are acknowledged after the idempotent event write completes.
                .thenRun(acknowledgment::acknowledge);
    }
}
