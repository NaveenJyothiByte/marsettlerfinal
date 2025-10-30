import java.time.LocalDateTime;
import java.util.*;

/**
 * Emergency management system for Mars Colony
 * Handles emergency alerts, priority escalation, and response coordination
 * Sprint 2 Feature: Emergency Response System
 */
public class EmergencyService {
    
    public enum EmergencyLevel {
        LOW,        // Routine maintenance issues
        MEDIUM,     // System malfunctions
        HIGH,       // Life support threats  
        CRITICAL    // Immediate colony danger
    }
    
    public enum EmergencyType {
        AIR_QUALITY,        // CO2 levels, oxygen issues
        POWER_FAILURE,      // Generator or solar array
        COMMUNICATION_LOSS, // Earth link disruption
        STRUCTURAL_BREACH,  // Habitat integrity
        MEDICAL_EMERGENCY,  // Crew health crisis
        RADIATION_SPIKE,    // Solar flare protection
        LIFE_SUPPORT        // Water, food, air systems
    }
    
    /**
     * Emergency Alert entity - represents an active emergency situation
     * Made public for testing access
     */
    public static class EmergencyAlert {
        public final String alertId;
        public final EmergencyType type;
        public final EmergencyLevel level;
        public final String location;
        public final String description;
        public final LocalDateTime timestamp;
        public boolean acknowledged;
        public String assignedTo; // User ID of responder
        
        public EmergencyAlert(String alertId, EmergencyType type, EmergencyLevel level, 
                             String location, String description) {
            this.alertId = alertId;
            this.type = type;
            this.level = level;
            this.location = location;
            this.description = description;
            this.timestamp = LocalDateTime.now();
            this.acknowledged = false;
            this.assignedTo = null;
        }
        
        // Getters for test access
        public String getAlertId() { return alertId; }
        public EmergencyType getType() { return type; }
        public EmergencyLevel getLevel() { return level; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isAcknowledged() { return acknowledged; }
        public String getAssignedTo() { return assignedTo; }
    }
    
    private final Map<String, EmergencyAlert> activeAlerts = new HashMap<>();
    private final List<EmergencyAlert> alertHistory = new ArrayList<>();
    private int nextAlertId = 1;
    
    /**
     * Creates new emergency alert and notifies appropriate personnel
     * Based on emergency level and type, different response protocols are triggered
     */
    public String createAlert(EmergencyType type, EmergencyLevel level, 
                             String location, String description) {
        String alertId = "ALERT-" + (nextAlertId++);
        EmergencyAlert alert = new EmergencyAlert(alertId, type, level, location, description);
        
        activeAlerts.put(alertId, alert);
        alertHistory.add(alert);
        
        // Auto-assign based on emergency type and level
        autoAssignResponder(alert);
        
        return alertId;
    }
    
    /**
     * Automatically assigns responders based on emergency characteristics
     * Critical emergencies alert all available personnel
     */
    private void autoAssignResponder(EmergencyAlert alert) {
        // In real implementation, this would query UserStore for available personnel
        // based on role, skills, and current location
        switch (alert.type) {
            case MEDICAL_EMERGENCY:
                alert.assignedTo = "MEDICAL_TEAM";
                break;
            case STRUCTURAL_BREACH:
            case LIFE_SUPPORT:
                alert.assignedTo = "ENGINEERING_TEAM";
                break;
            case COMMUNICATION_LOSS:
                alert.assignedTo = "COMMS_TEAM";
                break;
            default:
                alert.assignedTo = "GENERAL_RESPONSE";
        }
        
        if (alert.level == EmergencyLevel.CRITICAL) {
            alert.assignedTo = "ALL_AVAILABLE_PERSONNEL";
        }
    }
    
    /**
     * Acknowledges an emergency alert - indicates response is underway
     */
    public boolean acknowledgeAlert(String alertId, String userId) {
        EmergencyAlert alert = activeAlerts.get(alertId);
        if (alert != null) {
            alert.acknowledged = true;
            alert.assignedTo = userId;
            return true;
        }
        return false;
    }
    
    /**
     * Resolves an emergency - moves from active to history
     */
    public boolean resolveAlert(String alertId) {
        EmergencyAlert alert = activeAlerts.remove(alertId);
        return alert != null;
    }
    
    /**
     * Gets all active emergencies sorted by priority (critical first)
     */
    public List<EmergencyAlert> getActiveAlerts() {
        List<EmergencyAlert> alerts = new ArrayList<>(activeAlerts.values());
        // Sort by level - critical first
        Collections.sort(alerts, new Comparator<EmergencyAlert>() {
            @Override
            public int compare(EmergencyAlert a1, EmergencyAlert a2) {
                return a2.level.compareTo(a1.level); // Critical first
            }
        });
        return alerts;
    }
    
    /**
     * Gets emergencies of specific type and level threshold
     */
    public List<EmergencyAlert> getAlertsByPriority(EmergencyType type, EmergencyLevel minLevel) {
        List<EmergencyAlert> filteredAlerts = new ArrayList<>();
        for (EmergencyAlert alert : activeAlerts.values()) {
            if (alert.type == type && alert.level.compareTo(minLevel) >= 0) {
                filteredAlerts.add(alert);
            }
        }
        
        // Sort by level - critical first
        Collections.sort(filteredAlerts, new Comparator<EmergencyAlert>() {
            @Override
            public int compare(EmergencyAlert a1, EmergencyAlert a2) {
                return a2.level.compareTo(a1.level);
            }
        });
        return filteredAlerts;
    }
    
    /**
     * Emergency drill simulation for training purposes
     */
    public String simulateDrill(EmergencyType type, EmergencyLevel level) {
        return createAlert(type, level, "TRAINING_SIMULATION", 
                          "DRILL: " + type + " scenario - Level: " + level);
    }
    
    /**
     * Get alert by ID for testing purposes
     */
    public EmergencyAlert getAlert(String alertId) {
        return activeAlerts.get(alertId);
    }
    
    /**
     * Get alert history for analytics
     */
    public List<EmergencyAlert> getAlertHistory() {
        return new ArrayList<>(alertHistory);
    }
    
    /**
     * Clear all alerts (for testing cleanup)
     */
    public void clearAllAlerts() {
        activeAlerts.clear();
        alertHistory.clear();
        nextAlertId = 1;
    }
}