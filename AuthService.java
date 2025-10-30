import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * In-memory authentication + sessions + simple audit.
 * Handles login, session management, and security events.
 * Production would use proper password hashing and external session store.
 */
public class AuthService {
    
    private final Map<String, User> users; // keyed by normalized username (see UserStore)
    private final int maxAttempts;         // Lock threshold for brute force protection
    private final Duration sessionTimeout; // Automatic logout after inactivity
    
    // Security audit trail - in production would use proper logging framework
    private final List<String> audit = new ArrayList<>();
    private String lastError = null;       // Last error for UI feedback
    
    public AuthService(Map<String, User> users, int maxAttempts, Duration sessionTimeout) {
        if (users == null) throw new IllegalArgumentException("users map is null");
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be > 0");
        if (sessionTimeout == null || sessionTimeout.isZero() || sessionTimeout.isNegative())
            throw new IllegalArgumentException("sessionTimeout must be positive");
        
        this.users = users;
        this.maxAttempts = maxAttempts;
        this.sessionTimeout = sessionTimeout;
    }
    
    /** 
     * Returns user on success, else Optional.empty; sets lastError on failure.
     * Implements comprehensive security checks in order of increasing cost.
     */
    public Optional<User> login(String usernameKey, String password) {
        clearLastError();
        
        // Input validation - cheap checks first
        if (usernameKey == null || usernameKey.trim().isEmpty()) {
            setError("username is empty");
            audit.add("FAIL empty-username");
            return Optional.empty();
        }
        
        if (password == null) password = ""; // Normalize null passwords
        
        // User existence check
        User u = users.get(usernameKey);
        if (u == null) {
            setError("unknown user: " + usernameKey);
            audit.add("FAIL unknown-user:" + usernameKey);
            return Optional.empty();
        }
        
        // Account state checks
        if (u.status == AccountStatus.EXPIRED) {
            setError("account expired: " + u.username);
            audit.add("FAIL expired:" + u.username);
            return Optional.empty();
        }
        
        if (u.status == AccountStatus.LOCKED) {
            setError("account locked: " + u.username);
            audit.add("FAIL locked:" + u.username);
            return Optional.empty();
        }
        
        // Password verification - most expensive operation
        if (!Objects.equals(u.password, password)) {
            u.failedAttempts++;
            setError("invalid password (attempt " + u.failedAttempts + "): " + u.username);
            audit.add("FAIL bad-password:" + u.username + ":attempt=" + u.failedAttempts);
            
            // Auto-lock after too many failures
            if (u.failedAttempts >= maxAttempts) {
                u.status = AccountStatus.LOCKED;
                audit.add("LOCKED:" + u.username);
                setError("account locked after max attempts: " + u.username);
            }
            
            return Optional.empty();
        }
        
        // Successful login - reset security counters and update session
        u.failedAttempts = 0;
        u.loggedIn = true;
        u.lastActivity = Instant.now();
        audit.add("SUCCESS:" + u.username);
        return Optional.of(u);
    }
    
    /** Updates last activity timestamp to keep session alive */
    public void touch(User u) {
        if (u != null) u.lastActivity = Instant.now();
    }
    
    /** Checks if user session has expired due to inactivity */
    public boolean isSessionExpired(User u, Instant now) {
        if (u == null || !u.loggedIn || u.lastActivity == null) return true;
        if (now == null) now = Instant.now();
        return Duration.between(u.lastActivity, now).compareTo(sessionTimeout) > 0;
    }
    
    public void logout(User u) { 
        if (u != null) u.loggedIn = false; 
    }
    
    public List<String> getAudit() { 
        return Collections.unmodifiableList(audit); 
    }
    
    public String getLastError() { 
        return lastError; 
    }
    
    private void setError(String msg) { 
        lastError = msg; 
    }
    
    private void clearLastError() { 
        lastError = null; 
    }
}