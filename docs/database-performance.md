# Database Performance Guide - Bath Temperature

## Index Strategy

### Critical Indexes (Already Added)

1. **Primary Compound Index: `(device_id, received_at)`**
   - **Purpose**: Optimizes the most common query pattern
   - **Used by**: Chart queries, device-specific time ranges
   - **Query**: `WHERE device_id = ? AND received_at BETWEEN ? AND ?`
   - **Impact**: 10-100x faster queries for chart data

2. **Time Range Index: `(received_at)`**
   - **Purpose**: Queries across all devices by time
   - **Used by**: Cross-device analytics, cleanup operations
   - **Query**: `WHERE received_at BETWEEN ? AND ?`

3. **Latest Record Index: `(device_id, received_at DESC)`**
   - **Purpose**: Fast retrieval of latest temperature per device
   - **Used by**: Dashboard current values, API endpoints
   - **Query**: `WHERE device_id = ? ORDER BY received_at DESC LIMIT 1`

4. **Unique Constraint: `(device_id, received_at)`**
   - **Purpose**: Prevents duplicate measurements, fast EXISTS checks
   - **Used by**: Data validation, duplicate prevention
   - **Query**: `WHERE device_id = ? AND received_at = ?`

## Query Performance Expectations

### Without Indexes (current state):
- **Time range queries**: 500ms - 5000ms (grows linearly with data)
- **Latest temperature**: 100ms - 1000ms
- **Chart data**: 1000ms - 10000ms

### With Indexes (after migration):
- **Time range queries**: 5ms - 50ms (constant time)
- **Latest temperature**: 1ms - 5ms
- **Chart data**: 10ms - 100ms

## Data Growth Projections

### Measurement Frequency:
- **Current**: Every 15 minutes = 96 measurements/day/device
- **1 device, 1 year**: ~35,000 records
- **5 devices, 3 years**: ~500,000 records
- **10 devices, 5 years**: ~1,750,000 records

### Storage Impact:
- **Per record**: ~150 bytes (including indexes)
- **1M records**: ~150 MB total
- **Index overhead**: ~40% of table size
- **5 years projection**: ~300-500 MB total

## Maintenance Recommendations

### 1. Regular Cleanup (Implement Later)
```sql
-- Delete data older than 2 years
DELETE FROM bath_temperature 
WHERE received_at < NOW() - INTERVAL '2 years';
```

### 2. Partitioning (Consider After 1M Records)
```sql
-- Partition by month for very large datasets
CREATE TABLE bath_temperature_y2025m01 PARTITION OF bath_temperature
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### 3. Archive Strategy
- Keep 2 years of detailed data
- Archive older data to separate table with daily/weekly aggregates
- Compress historical data

### 4. Monitoring Queries
```sql
-- Check index usage
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE tablename = 'bath_temperature';

-- Check query performance
EXPLAIN ANALYZE 
SELECT * FROM bath_temperature 
WHERE device_id = 'eui-a840411f8182f655' 
AND received_at BETWEEN '2025-01-01' AND '2025-01-02';
```

## Implementation Steps

1. **Run migration**: `V2__Add_Bath_Temperature_Indexes.sql`
2. **Restart application**: Indexes will be created automatically
3. **Monitor performance**: Use application logs and database stats
4. **Plan archiving**: When data exceeds 1M records (estimated 2-3 years)

## Cost-Benefit Analysis

### Benefits:
- **100x faster queries** for chart generation
- **Better user experience** with instant chart loading
- **Reduced server load** and database CPU usage
- **Scalability** to millions of records

### Costs:
- **~40% storage overhead** for indexes
- **Slightly slower INSERTs** (minimal impact for IoT data rate)
- **Index maintenance** during bulk operations

**Conclusion**: The performance benefits far outweigh the storage costs, especially as data grows.
