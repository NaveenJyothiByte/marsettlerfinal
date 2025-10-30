/**
 * Tracks account lifecycle states
 * ACTIVE: Normal operational state
 * EXPIRED: Password or subscription expired
 * LOCKED: Temporarily disabled due to security concerns
 */
public enum AccountStatus {
    ACTIVE,
    EXPIRED, 
    LOCKED
}