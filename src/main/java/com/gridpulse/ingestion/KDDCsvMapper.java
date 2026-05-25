package com.gridpulse.ingestion;

import com.gridpulse.model.NetworkEvent;
import com.gridpulse.model.Severity;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KDDCsvMapper {
    private static final int KDD_FIELD_COUNT = 42;

    public NetworkEvent map(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < KDD_FIELD_COUNT) {
            throw new IllegalArgumentException("KDD row must contain at least 42 columns");
        }

        long duration = parseLong(fields[0]);
        String protocolType = fields[1];
        String flag = fields[3];
        long srcBytes = parseLong(fields[4]);
        long dstBytes = parseLong(fields[5]);
        String label = fields[fields.length - 1];

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", duration);
        metadata.put("trafficVolume", srcBytes + dstBytes);
        metadata.put("srcBytes", srcBytes);
        metadata.put("dstBytes", dstBytes);
        metadata.put("kddFlag", flag);

        NetworkEvent event = new NetworkEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setAssetId(assetId(protocolType, srcBytes));
        event.setTimestamp(Instant.now());
        event.setEventType(protocolType);
        event.setSeverity(mapSeverity(flag));
        event.setMetadata(metadata);
        event.setKddLabel(label);
        return event;
    }

    private String assetId(String protocolType, long srcBytes) {
        int bucket = Math.floorMod((protocolType + ":" + srcBytes).hashCode(), 20_000);
        return "asset-" + (bucket + 1);
    }

    private Severity mapSeverity(String flag) {
        return switch (flag == null ? "" : flag.trim().toUpperCase()) {
            case "SF" -> Severity.LOW;
            case "REJ" -> Severity.MEDIUM;
            case "S0", "S1", "S2", "S3" -> Severity.HIGH;
            case "ERROR" -> Severity.CRITICAL;
            default -> Severity.MEDIUM;
        };
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
