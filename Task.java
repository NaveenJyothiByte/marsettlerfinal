import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled task in the Mars Colony
 * Tasks are time-bound and have priority levels for sorting
 */
public class Task {
    
    public final String id;        // Unique task identifier
    public final String title;     // Human-readable description
    /** 1 = highest priority, 5 = lowest */
    public final int priority;     // Critical for emergency response ordering
    public final LocalDate date;   // When this task occurs
    public final LocalTime time;   // Specific time of day
    
    public Task(String id, String title, int priority, LocalDate date, LocalTime time) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.time = time;
    }
    
    @Override
    public String toString() {
        return String.format("[%s %s] p%d %s", date, time, priority, title);
    }
}