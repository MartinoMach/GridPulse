package com.gridpulse.ingestion;

import com.gridpulse.config.GridPulseProperties;
import com.gridpulse.model.NetworkEvent;
import com.gridpulse.model.Severity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NetworkEventSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkEventSimulator.class);

    private final GridPulseProperties properties;
    private final KafkaTemplate<String, NetworkEvent> kafkaTemplate;
    private final Random random = new Random();
    private final AtomicLong emitted = new AtomicLong();
    private ScheduledExecutorService scheduler;

    public NetworkEventSimulator(GridPulseProperties properties, KafkaTemplate<String, NetworkEvent> kafkaTemplate) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void startWhenEnabled() {
        if (!properties.simulator().enabled()) {
            return;
        }
        scheduler = Executors.newScheduledThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        scheduler.scheduleAtFixedRate(this::emitBurst, 0, 100, TimeUnit.MILLISECONDS);
        LOGGER.info("Fallback simulator started at {} events/sec", properties.simulator().targetEventsPerSecond());
    }

    private void emitBurst() {
        int perTick = Math.max(1, properties.simulator().targetEventsPerSecond() / 10);
        for (int i = 0; i < perTick; i++) {
            NetworkEvent event = syntheticEvent();
            kafkaTemplate.send(properties.kafka().networkEventsTopic(), event.getAssetId(), event);
            emitted.incrementAndGet();
        }
    }

    private NetworkEvent syntheticEvent() {
        int assetNumber = 1 + random.nextInt(20_000);
        double thirtyMinuteRamp = (System.currentTimeMillis() % TimeUnit.MINUTES.toMillis(30))
                / (double) TimeUnit.MINUTES.toMillis(30);
        boolean troubledAsset = assetNumber % 17 == 0;
        Severity severity = severityFor(troubledAsset ? thirtyMinuteRamp : random.nextDouble() * 0.35);
        long trafficVolume = troubledAsset
                ? Math.round(50_000 + (thirtyMinuteRamp * 1_750_000) + random.nextInt(60_000))
                : 2_000 + random.nextInt(80_000);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", random.nextInt(240));
        metadata.put("trafficVolume", trafficVolume);
        metadata.put("simulatedRamp", thirtyMinuteRamp);

        NetworkEvent event = new NetworkEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setAssetId("asset-" + assetNumber);
        event.setTimestamp(Instant.now());
        event.setEventType(random.nextBoolean() ? "tcp" : "udp");
        event.setSeverity(severity);
        event.setMetadata(metadata);
        event.setKddLabel(troubledAsset && thirtyMinuteRamp > 0.75 ? "simulated_outage." : "normal.");
        return event;
    }

    private Severity severityFor(double ramp) {
        if (ramp > 0.92) {
            return Severity.CRITICAL;
        }
        if (ramp > 0.68) {
            return Severity.HIGH;
        }
        if (ramp > 0.35) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
