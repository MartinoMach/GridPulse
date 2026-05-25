package com.gridpulse;

import static org.assertj.core.api.Assertions.assertThat;

import com.gridpulse.ingestion.KDDCsvMapper;
import com.gridpulse.model.NetworkEvent;
import com.gridpulse.model.Severity;
import org.junit.jupiter.api.Test;

class KDDCsvMapperTest {
    @Test
    void mapsKddFieldsToNetworkEventSchema() {
        String row = "0,tcp,http,SF,181,5450,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,8,8,0.00,0.00,0.00,0.00,1.00,0.00,0.00,9,9,1.00,0.00,0.11,0.00,0.00,0.00,0.00,0.00,normal.";

        NetworkEvent event = new KDDCsvMapper().map(row);

        assertThat(event.getEventType()).isEqualTo("tcp");
        assertThat(event.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(event.getMetadata()).containsEntry("duration", 0L);
        assertThat(event.getMetadata()).containsEntry("trafficVolume", 5631L);
        assertThat(event.getKddLabel()).isEqualTo("normal.");
        assertThat(event.getAssetId()).startsWith("asset-");
    }
}
