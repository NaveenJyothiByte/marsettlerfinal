import java.time.Instant;

/**
 * Core user entity representing a Mars Colony member
 * Contains authentication, role, and session tracking data
 */
public class User {
    
    // Immutable identifiers
    public final String userId;      // Unique system identifier (e.g., "U1001")
    public final String username;    // Login name (stored as given; UserStore uses normalized keys)
    public final Role role;          // Security and access permissions
    
    // Mutable security fields
    public String password;          // Hashed in production - plain text for prototype
    public AccountStatus status;     // Current account state
    
    // Security tracking - resets on successful login
    public int failedAttempts = 0;
    
    // Session management
    public boolean loggedIn = false;
    public Instant lastActivity = null;  // Used for automatic session timeout
    
    public User(String userId, String username, String password, 
                AccountStatus status, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
    }
}