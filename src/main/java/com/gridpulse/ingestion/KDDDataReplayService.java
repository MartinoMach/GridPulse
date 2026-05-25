package com.gridpulse.ingestion;

import com.gridpulse.config.GridPulseProperties;
import com.gridpulse.model.NetworkEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KDDDataReplayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KDDDataReplayService.class);

    private final GridPulseProperties properties;
    private final KafkaTemplate<String, NetworkEvent> kafkaTemplate;
    private final KDDCsvMapper mapper = new KDDCsvMapper();
    private final AtomicLong published = new AtomicLong();
    private final AtomicLong lastLoggedCount = new AtomicLong();
    private ScheduledExecutorService scheduler;
    private BufferedReader reader;

    public KDDDataReplayService(GridPulseProperties properties, KafkaTemplate<String, NetworkEvent> kafkaTemplate) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void startWhenEnabled() throws IOException {
        if (!properties.replay().enabled()) {
            return;
        }

        reader = Files.newBufferedReader(Path.of(properties.replay().csvPath()));
        scheduler = Executors.newScheduledThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        int tickMillis = properties.replay().tickMillis();
        scheduler.scheduleAtFixedRate(this::publishTick, 0, tickMillis, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::logThroughput, 10, 10, TimeUnit.SECONDS);
        LOGGER.info("KDD replay started from {} at {} events/sec",
                properties.replay().csvPath(),
                properties.replay().targetEventsPerSecond());
    }

    private void publishTick() {
        int eventsThisTick = Math.max(1,
                properties.replay().targetEventsPerSecond() * properties.replay().tickMillis() / 1000);
        for (int i = 0; i < eventsThisTick; i++) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    LOGGER.info("KDD replay completed after {} events", published.get());
                    shutdown();
                    return;
                }
                NetworkEvent event = mapper.map(line);
                kafkaTemplate.send(properties.kafka().networkEventsTopic(), event.getAssetId(), event);
                published.incrementAndGet();
            } catch (Exception ex) {
                LOGGER.warn("Skipping malformed KDD row during replay: {}", ex.getMessage());
            }
        }
    }

    private void logThroughput() {
        long current = published.get();
        long previous = lastLoggedCount.getAndSet(current);
        LOGGER.info("KDD replay progress: total={} throughput={} events/sec", current, (current - previous) / 10);
    }

    @PreDestroy
    public void shutdown() throws IOException {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (reader != null) {
            reader.close();
        }
    }
}
