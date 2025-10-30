/**
 * User roles in the Mars Colony system.
 * Defines three distinct roles with different access levels.
 */
public enum Role {
    
    COLONY_RESIDENT,           // Basic colony member with personal schedule access
    MISSION_CONTROL_OPERATOR,  // Can view and manage colony-wide operations
    INFRASTRUCTURE_TECHNICIAN; // Maintains and monitors colony systems
    
    /**
     * Converts enum to human-readable format for UI display
     */
    public static String pretty(Role r) {
        switch (r) {
            case COLONY_RESIDENT: return "Colony Resident";
            case MISSION_CONTROL_OPERATOR: return "Mission Control Operator";
            case INFRASTRUCTURE_TECHNICIAN: return "Infrastructure Technician";
            default: return r.name(); // fallback
        }
    }
}