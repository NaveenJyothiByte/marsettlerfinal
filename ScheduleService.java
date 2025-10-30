import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory schedule per user, sorted reads, last update timestamp.
 * Production would use database with proper transactions.
 */
public class ScheduleService {
    
    // Nested structure: User -> Date -> List of Tasks
    private final Map<String, Map<LocalDate, List<Task>>> store = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastUpdate = new ConcurrentHashMap<>();
    
    /**
     * Pre-populates sample tasks for new colony residents
     * Demonstrates realistic Mars colony daily operations
     */
    public void seedResidentTasks(String username) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, 
            k -> new ConcurrentHashMap<>());
        
        LocalDate today = LocalDate.now();
        
        // Today's critical colony maintenance tasks
        cal.put(today, new ArrayList<>(Arrays.asList(
            new Task("t1", "Inspect hydroponics", 2, today, 
                    java.time.LocalTime.of(9, 0)),
            new Task("t2", "Check airlock seals", 1, today,  // High priority - safety
                    java.time.LocalTime.of(10, 30)),
            new Task("t3", "Rover battery check", 3, today, 
                    java.time.LocalTime.of(13, 0))
        )));
        
        // Tomorrow's planned activities
        cal.put(today.plusDays(1), new ArrayList<>(Arrays.asList(
            new Task("t4", "Soil sample catalog", 2, today.plusDays(1), 
                    java.time.LocalTime.of(11, 0))
        )));
        
        lastUpdate.put(username, Instant.now());
    }
    
    /**
     * Retrieves tasks for a user on specific date, sorted by time then priority
     * Returns defensive copy to prevent external modification
     */
    public List<Task> getTasks(String username, LocalDate date) {
        List<Task> list = store
            .getOrDefault(username, Collections.emptyMap())
            .getOrDefault(date, new ArrayList<Task>());
        
        // Create sorted copy - original remains unsorted for performance
        List<Task> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing((Task t) -> t.time).thenComparingInt(t -> t.priority));
        return copy;
    }
    
    /**
     * Upsert operation: inserts new task or updates existing one by ID
     * Atomic operation per user with timestamp update
     */
    public void upsertTask(String username, Task task) {
        Map<LocalDate, List<Task>> cal = store.computeIfAbsent(username, 
            k -> new ConcurrentHashMap<>());
        
        List<Task> day = cal.computeIfAbsent(task.date, k -> new ArrayList<>());
        
        // Remove existing task with same ID (update case)
        day.removeIf(t -> t.id.equals(task.id));
        day.add(task);
        
        lastUpdate.put(username, Instant.now());
    }
    
    public Instant getLastUpdate(String username) {
        return lastUpdate.getOrDefault(username, Instant.EPOCH);
    }
    
    /**
     * Creates deep copy snapshot for backup operations
     * Expensive operation - use judiciously
     */
    public Map<String, Map<LocalDate, List<Task>>> snapshot() {
        Map<String, Map<LocalDate, List<Task>>> snap = new HashMap<>();
        
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : store.entrySet()) {
            Map<LocalDate, List<Task>> perDay = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                perDay.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            snap.put(e.getKey(), perDay);
        }
        return snap;
    }
    
    /** Restores from backup snapshot - replaces entire schedule store */
    public void restore(Map<String, Map<LocalDate, List<Task>>> snap) {
        store.clear();
        if (snap == null) return;
        
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : snap.entrySet()) {
            Map<LocalDate, List<Task>> perDay = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                perDay.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            store.put(e.getKey(), perDay);
        }
    }
}