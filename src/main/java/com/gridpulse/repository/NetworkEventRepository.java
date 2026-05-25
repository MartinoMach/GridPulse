package com.gridpulse.repository;

import com.gridpulse.model.NetworkEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NetworkEventRepository extends JpaRepository<NetworkEvent, String> {
    @Query("""
            select e
            from NetworkEvent e
            where e.assetId = :assetId
              and e.timestamp >= :windowStart
            order by e.timestamp desc
            """)
    List<NetworkEvent> findRecentEventsByAssetWithinWindow(
            @Param("assetId") String assetId,
            @Param("windowStart") Instant windowStart);

    @Query("""
            select distinct e.assetId
            from NetworkEvent e
            where e.timestamp >= :windowStart
            """)
    List<String> findActiveAssetIds(@Param("windowStart") Instant windowStart);
}
