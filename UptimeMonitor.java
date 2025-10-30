/**
 * Tracks system reliability metrics
 * Simulates uptime calculations for colony operations reporting
 */
public class UptimeMonitor {
    
    private long totalMillis;
    private long downtimeMillis;
    
    /**
     * Simulates system uptime over a period with injected downtime
     * @param totalMs Total observation period in milliseconds
     * @param injectedDowntimeMs Simulated downtime within that period
     */
    public void simulate(long totalMs, long injectedDowntimeMs) {
        this.totalMillis = totalMs;
        this.downtimeMillis = injectedDowntimeMs;
    }
    
    /**
     * Calculates uptime percentage - critical for colony reliability reporting
     * @return Uptime percentage (99.9+% target for life support systems)
     */
    public double uptimePercent() {
        if (totalMillis <= 0) return 0.0;
        return 100.0 * (totalMillis - downtimeMillis) / totalMillis;
    }
}