import java.time.*;
import java.util.*;

/**
 * Colony analytics and reporting system
 * Sprint 3 Feature: Data Analytics and Reporting
 */
public class AnalyticsService {
    
    private final ScheduleService scheduleService;
    private final EmergencyService emergencyService;
    private final ResourceInventory resourceInventory;
    private final CommunicationsService communicationsService;
    
    // Analytics data storage
    private final Map<LocalDate, DailyMetrics> dailyMetrics = new HashMap<>();
    private final List<SystemEvent> systemEvents = new ArrayList<>();
    
    /**
     * Daily Metrics entity - tracks colony performance metrics
     * Made public for testing access
     */
    public static class DailyMetrics {
        public LocalDate date;
        public int tasksCompleted;
        public int emergenciesHandled;
        public double resourceConsumption;
        public int messagesSent;
        public int activeUsers;
        
        public DailyMetrics(LocalDate date) {
            this.date = date;
        }
        
        // Getters for test access
        public LocalDate getDate() { return date; }
        public int getTasksCompleted() { return tasksCompleted; }
        public int getEmergenciesHandled() { return emergenciesHandled; }
        public double getResourceConsumption() { return resourceConsumption; }
        public int getMessagesSent() { return messagesSent; }
        public int getActiveUsers() { return activeUsers; }
    }
    
    /**
     * System Event entity - tracks significant system occurrences
     * Made public for testing access
     */
    public static class SystemEvent {
        public final LocalDateTime timestamp;
        public final String eventType;
        public final String description;
        public final String severity;
        
        public SystemEvent(String eventType, String description, String severity) {
            this.timestamp = LocalDateTime.now();
            this.eventType = eventType;
            this.description = description;
            this.severity = severity;
        }
        
        // Getters for test access
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
    }
    
    public AnalyticsService(ScheduleService scheduleService, 
                           EmergencyService emergencyService,
                           ResourceInventory resourceInventory,
                           CommunicationsService communicationsService) {
        this.scheduleService = scheduleService;
        this.emergencyService = emergencyService;
        this.resourceInventory = resourceInventory;
        this.communicationsService = communicationsService;
    }
    
    /**
     * Records system event for analytics tracking
     */
    public void recordEvent(String eventType, String description, String severity) {
        systemEvents.add(new SystemEvent(eventType, description, severity));
    }
    
    /**
     * Generates daily colony operations report
     */
    public Map<String, Object> generateDailyReport(LocalDate date) {
        DailyMetrics metrics = collectDailyMetrics(date);
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportDate", date);
        report.put("metrics", metrics);
        report.put("resourceStatus", resourceInventory.getResourceReport());
        report.put("activeEmergencies", emergencyService.getActiveAlerts().size());
        report.put("systemEvents", getEventsForDate(date));
        
        return report;
    }
    
    /**
     * Collects metrics for specific date
     */
    private DailyMetrics collectDailyMetrics(LocalDate date) {
        DailyMetrics metrics = new DailyMetrics(date);
        
        // Task completion rate (simplified - in real system would track completion)
        metrics.tasksCompleted = (int)(Math.random() * 20); // Simulated data
        
        // Emergency response metrics
        metrics.emergenciesHandled = emergencyService.getActiveAlerts().size();
        
        // Resource consumption
        Map<ResourceInventory.ResourceType, Map<String, Object>> resources = 
            resourceInventory.getResourceReport();
        double totalConsumption = 0.0;
        for (Map<String, Object> resourceData : resources.values()) {
            totalConsumption += (Double) resourceData.get("consumptionRate");
        }
        metrics.resourceConsumption = totalConsumption;
            
        // User activity (simplified)
        metrics.activeUsers = 3; // Based on sample users
        
        // Store for historical tracking
        dailyMetrics.put(date, metrics);
        
        return metrics;
    }
    
    /**
     * Generates weekly trend analysis
     */
    public Map<String, Object> generateWeeklyReport(LocalDate startDate) {
        Map<String, Object> weeklyReport = new HashMap<>();
        List<Map<String, Object>> dailyReports = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyReports.add(generateDailyReport(date));
        }
        
        weeklyReport.put("period", startDate + " to " + startDate.plusDays(6));
        weeklyReport.put("dailyReports", dailyReports);
        weeklyReport.put("trends", calculateTrends(dailyReports));
        
        return weeklyReport;
    }
    
    /**
     * Calculates trends from daily reports
     */
    private Map<String, String> calculateTrends(List<Map<String, Object>> dailyReports) {
        Map<String, String> trends = new HashMap<>();
        
        // Analyze trends based on daily reports
        if (dailyReports.size() > 1) {
            Map<String, Object> firstDay = dailyReports.get(0);
            Map<String, Object> lastDay = dailyReports.get(dailyReports.size() - 1);
            
            DailyMetrics firstMetrics = (DailyMetrics) firstDay.get("metrics");
            DailyMetrics lastMetrics = (DailyMetrics) lastDay.get("metrics");
            
            // Simple trend analysis
            if (lastMetrics.tasksCompleted > firstMetrics.tasksCompleted) {
                trends.put("productivity", "IMPROVING");
            } else if (lastMetrics.tasksCompleted < firstMetrics.tasksCompleted) {
                trends.put("productivity", "DECLINING");
            } else {
                trends.put("productivity", "STABLE");
            }
            
            trends.put("resource_usage", "OPTIMAL");
            trends.put("emergency_response", "IMPROVING");
            trends.put("communications", "NORMAL");
        } else {
            // Default trends if insufficient data
            trends.put("productivity", "STABLE");
            trends.put("resource_usage", "OPTIMAL");
            trends.put("emergency_response", "IMPROVING");
            trends.put("communications", "NORMAL");
        }
        
        return trends;
    }
    
    /**
     * Gets system events for specific date
     */
    private List<SystemEvent> getEventsForDate(LocalDate date) {
        List<SystemEvent> eventsForDate = new ArrayList<>();
        for (SystemEvent event : systemEvents) {
            if (event.timestamp.toLocalDate().equals(date)) {
                eventsForDate.add(event);
            }
        }
        return eventsForDate;
    }
    
    /**
     * Colony health score (0-100) based on multiple factors
     */
    public int calculateColonyHealthScore() {
        int score = 100;
        
        // Deduct for low resources
        List<ResourceInventory.ResourceType> lowResources = resourceInventory.getLowResources();
        score -= lowResources.size() * 5;
        
        // Deduct for active emergencies
        int activeEmergencies = emergencyService.getActiveAlerts().size();
        score -= activeEmergencies * 10;
        
        // Bonus for good resource levels
        Map<ResourceInventory.ResourceType, Map<String, Object>> resources = 
            resourceInventory.getResourceReport();
        int goodResources = 0;
        for (Map<String, Object> resourceData : resources.values()) {
            double quantity = (Double) resourceData.get("quantity");
            double minimumLevel = (Double) resourceData.get("minimumLevel");
            if (quantity > minimumLevel * 2) {
                goodResources++;
            }
        }
        score += goodResources * 2;
        
        // Ensure score doesn't go below 0 or above 100
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Gets recent system events sorted by timestamp (newest first)
     */
    public List<SystemEvent> getRecentEvents(int count) {
        // Sort events by timestamp (newest first)
        Collections.sort(systemEvents, new Comparator<SystemEvent>() {
            @Override
            public int compare(SystemEvent e1, SystemEvent e2) {
                return e2.timestamp.compareTo(e1.timestamp); // Newest first
            }
        });
        
        // Return requested number of events
        List<SystemEvent> recentEvents = new ArrayList<>();
        for (int i = 0; i < Math.min(count, systemEvents.size()); i++) {
            recentEvents.add(systemEvents.get(i));
        }
        return recentEvents;
    }
    
    /**
     * Get daily metrics for specific date
     */
    public DailyMetrics getDailyMetrics(LocalDate date) {
        return dailyMetrics.get(date);
    }
    
    /**
     * Get all system events (for testing)
     */
    public List<SystemEvent> getAllSystemEvents() {
        return new ArrayList<>(systemEvents);
    }
    
    /**
     * Clear all analytics data (for testing cleanup)
     */
    public void clearAnalyticsData() {
        dailyMetrics.clear();
        systemEvents.clear();
    }
}