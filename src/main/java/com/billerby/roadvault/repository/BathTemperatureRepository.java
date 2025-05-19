package com.billerby.roadvault.repository;

import com.billerby.roadvault.model.BathTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for handling bath temperature data operations.
 */
@Repository
public interface BathTemperatureRepository extends JpaRepository<BathTemperature, Long> {

    /**
     * Find all temperature records for a specific device
     * @param deviceId The device ID to filter by
     * @return List of temperature records
     */
    List<BathTemperature> findByDeviceIdOrderByReceivedAtDesc(String deviceId);

    /**
     * Find temperature records within a time range
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return List of temperature records
     */
    List<BathTemperature> findByReceivedAtBetweenOrderByReceivedAtDesc(Instant startTime, Instant endTime);

    /**
     * Find temperature records for a specific device within a time range
     * @param deviceId The device ID to filter by
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return List of temperature records
     */
    List<BathTemperature> findByDeviceIdAndReceivedAtBetweenOrderByReceivedAtDesc(
            String deviceId, Instant startTime, Instant endTime);

    /**
     * Get the latest temperature record for a specific device
     * @param deviceId The device ID to filter by
     * @return The latest temperature record
     */
    BathTemperature findFirstByDeviceIdOrderByReceivedAtDesc(String deviceId);

    /**
     * Get the latest temperature record regardless of device
     * @return The latest temperature record
     */
    BathTemperature findFirstByOrderByReceivedAtDesc();
    
    /**
     * Check if a temperature record already exists for a specific device and time
     * @param deviceId The device ID to filter by
     * @param receivedAt The received timestamp
     * @return True if a record exists, false otherwise
     */
    boolean existsByDeviceIdAndReceivedAt(String deviceId, Instant receivedAt);

    /**
     * Get daily average temperatures for a specific device within a time range
     * This is a custom query that groups the data by day.
     */
    @Query(value = "SELECT DATE(bt.received_at) as date, AVG(bt.temperature) as avg_temp " +
            "FROM bath_temperature bt " +
            "WHERE bt.device_id = :deviceId " +
            "AND bt.received_at BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(bt.received_at) " +
            "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> getDailyAverageTemperatures(
            @Param("deviceId") String deviceId, 
            @Param("startTime") Instant startTime, 
            @Param("endTime") Instant endTime);

    /**
     * Get hourly average temperatures for a specific device within a time range
     * This is a custom query that groups the data by hour.
     */
    @Query(value = "SELECT DATE_FORMAT(bt.received_at, '%Y-%m-%d %H:00:00') as hour, AVG(bt.temperature) as avg_temp " +
            "FROM bath_temperature bt " +
            "WHERE bt.device_id = :deviceId " +
            "AND bt.received_at BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE_FORMAT(bt.received_at, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour DESC", nativeQuery = true)
    List<Object[]> getHourlyAverageTemperatures(
            @Param("deviceId") String deviceId, 
            @Param("startTime") Instant startTime, 
            @Param("endTime") Instant endTime);
}
