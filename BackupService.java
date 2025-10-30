import java.time.LocalDate;
import java.util.*;

/**
 * Simple backup/restore service for schedule data
 * In production would serialize to persistent storage
 */
public class BackupService {
    
    private Map<String, Map<LocalDate, List<Task>>> snapshot;
    
    /**
     * Creates deep copy backup of schedule data
     * Not thread-safe - callers should synchronize if needed
     */
    public void backup(Map<String, Map<LocalDate, List<Task>>> source) {
        snapshot = new HashMap<>();
        if (source == null) return;
        
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : source.entrySet()) {
            Map<LocalDate, List<Task>> days = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                days.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            snapshot.put(e.getKey(), days);
        }
    }
    
    /**
     * Restores from last backup - returns defensive copy
     */
    public Map<String, Map<LocalDate, List<Task>>> restore() {
        Map<String, Map<LocalDate, List<Task>>> restored = new HashMap<>();
        if (snapshot == null) return restored;
        
        for (Map.Entry<String, Map<LocalDate, List<Task>>> e : snapshot.entrySet()) {
            Map<LocalDate, List<Task>> days = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> d : e.getValue().entrySet()) {
                days.put(d.getKey(), new ArrayList<>(d.getValue()));
            }
            restored.put(e.getKey(), days);
        }
        return restored;
    }
}