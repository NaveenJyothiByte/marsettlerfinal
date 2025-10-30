import java.util.*;

/**
 * In-memory user database with normalization
 * Production would use proper database with hashed passwords
 */
public class UserStore {
    
    private final Map<String, User> users = new HashMap<>();
    private int nextUserNum = 2000; // Sequential user ID generation
    
    /**
     * Normalizes usernames for case-insensitive lookups
     * Trims and converts to lowercase for consistency
     */
    public String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }
    
    public Map<String, User> backingMap() { return users; }
    
    public boolean exists(String usernameRaw) { 
        return users.containsKey(normalize(usernameRaw)); 
    }
    
    public User get(String usernameRaw) { 
        return users.get(normalize(usernameRaw)); 
    }
    
    /**
     * Creates new user account with sequential ID generation
     * Auto-assigns user ID in U2000+ range
     */
    public User addNew(String usernameRaw, String password, AccountStatus status, Role role) {
        String key = normalize(usernameRaw);
        String userId = "U" + (nextUserNum++);
        User u = new User(userId, key, password, status, role);
        users.put(key, u);
        return u;
    }
    
    /** 
     * Seed three ACTIVE users so they can log in immediately 
     * Demonstrates different roles and test scenarios
     */
    public void seedSamples() {
        // Valid active resident
        users.put(normalize("resident.valid@mars.local"),
            new User("U1001", "resident.valid@mars.local", "Passw0rd!", 
                    AccountStatus.ACTIVE, Role.COLONY_RESIDENT));
        
        // Expired account scenario
        users.put(normalize("resident.expired@mars.local"),
            new User("U1002", "resident.expired@mars.local", "AnyPass", 
                    AccountStatus.ACTIVE, Role.MISSION_CONTROL_OPERATOR));
        
        // Locked account scenario  
        users.put(normalize("resident.locked@mars.local"),
            new User("U1003", "resident.locked@mars.local", "Pass123", 
                    AccountStatus.ACTIVE, Role.INFRASTRUCTURE_TECHNICIAN));
    }
}