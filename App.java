import java.time.*;
import java.util.*;

/**
 * Main application controller - coordinates all system components
 * Handles user interaction flow and menu navigation
 */
public class App {
    
    private final UserStore store = new UserStore();
    private AuthService auth;
    private ScheduleService schedule;
    private BackupService backup;
    private UptimeMonitor uptime;
    private User currentUser = null;
    
    /**
     * Application entry point - initializes system and starts main loop
     */
    public void run() {
        bootstrap();
        loop();
        System.out.println("Goodbye!");
    }
    
    /**
     * Initializes all system components with default data
     * Seeds sample users and demo schedules
     */
    private void bootstrap() {
        store.seedSamples();
        auth = new AuthService(store.backingMap(), 5, Duration.ofMinutes(15));
        schedule = new ScheduleService();
        backup = new BackupService();
        uptime = new UptimeMonitor();
        
        // Pre-populate sample tasks for demo user
        schedule.seedResidentTasks("resident.valid@mars.local");
    }
    
    /**
     * Main application loop - handles menu navigation and user input
     */
    private void loop() {
        while (true) {
            printHeader();
            
            if (!isLoggedIn()) {
                // Pre-login menu
                System.out.println("1) Login");
                System.out.println("2) Create account");
                System.out.println("11) Exit");
                
                int c = Input.intVal("Choose an option: ");
                switch (c) {
                    case 1: doLogin(); break;
                    case 2: doCreateAccount(); break;
                    case 11: return;
                    default: System.out.println("Invalid option."); Input.pause();
                }
            } else {
                // Post-login menu
                System.out.println("2) View schedule by date");
                System.out.println("3) Add/Update a task");
                System.out.println("4) Show last schedule update timestamp");
                System.out.println("5) Simulate inactivity (minutes)");
                System.out.println("6) Show audit log");
                System.out.println("7) Backup schedules");
                System.out.println("8) Restore schedules from last backup");
                System.out.println("9) Simulate uptime (total hours + downtime minutes)");
                System.out.println("10) Logout");
                System.out.println("11) Exit");
                
                int c = Input.intVal("Choose an option: ");
                switch (c) {
                    case 2: doViewSchedule(); break;
                    case 3: doAddOrUpdateTask(); break;
                    case 4: showLastUpdate(); break;
                    case 5: simulateInactivity(); break;
                    case 6: showAuditLog(); break;
                    case 7: doBackup(); break;
                    case 8: doRestore(); break;
                    case 9: simulateUptime(); break;
                    case 10: doLogout(); break;
                    case 11: return;
                    default: System.out.println("Invalid option."); Input.pause();
                }
            }
        }
    }
    
    /**
     * Displays application header with current user status
     * Shows session expiry warning when appropriate
     */
    private void printHeader() {
        System.out.println("\n================ Mars Settler (Sprint 1) ================");
        
        if (isLoggedIn()) {
            boolean expired = auth.isSessionExpired(currentUser, Instant.now());
            System.out.println("Logged in as: " + currentUser.username +
                               " | User ID: " + currentUser.userId +
                               " | Role: " + Role.pretty(currentUser.role) +
                               (expired ? " (SESSION EXPIRED)" : ""));
        } else {
            System.out.println("Not logged in");
        }
    }
    
    private boolean isLoggedIn() { 
        return currentUser != null && currentUser.loggedIn; 
    }
    
    /**
     * Handles user login process with comprehensive error feedback
     */
    private void doLogin() {
        if (isLoggedIn()) { 
            System.out.println("Already logged in."); 
            Input.pause(); 
            return; 
        }
        
        String usernameKey = store.normalize(Input.line("Username: "));
        String password = Input.line("Password: ");
        
        Optional<User> u = auth.login(usernameKey, password);
        
        if (u.isPresent()) {
            currentUser = u.get();
            System.out.println("Login successful. Role: " + 
                Role.pretty(currentUser.role) + " | User ID: " + currentUser.userId);
        } else {
            System.out.println("Login failed.");
            // Show last audit entry for debugging
            List<String> log = auth.getAudit();
            if (!log.isEmpty()) 
                System.out.println("Audit: " + log.get(log.size() - 1));
        }
        
        Input.pause();
    }
    
    /**
     * Handles new account creation with automatic login
     */
    private void doCreateAccount() {
        if (isLoggedIn()) { 
            System.out.println("Already logged in."); 
            Input.pause(); 
            return; 
        }
        
        String username = store.normalize(Input.line("New username (email-like string): "));
        if (store.exists(username)) { 
            System.out.println("Account already exists. Please login."); 
            Input.pause(); 
            return; 
        }
        
        String password = Input.line("New password: ");
        Role role = promptRole();
        
        User u = store.addNew(username, password, AccountStatus.ACTIVE, role);
        System.out.println("Account created: " + u.username + 
                          " | User ID: " + u.userId + 
                          " | Role: " + Role.pretty(role));
        
        // Auto-login after account creation
        Optional<User> logged = auth.login(u.username, password);
        if (logged.isPresent()) {
            currentUser = logged.get();
            System.out.println("Logged in as " + currentUser.username + 
                              " (User ID: " + currentUser.userId + ")");
            
            // Seed sample tasks for new colony residents
            if (currentUser.role == Role.COLONY_RESIDENT)
                schedule.seedResidentTasks(currentUser.username);
        } else {
            System.out.println("Unexpected: auto-login failed.");
        }
        
        Input.pause();
    }
    
    /**
     * Role selection prompt with validation
     */
    private Role promptRole() {
        while (true) {
            System.out.println("\nSelect account type:");
            System.out.println("1) Colony Residents");
            System.out.println("2) Mission Control Operators");
            System.out.println("3) Infrastructure Technicians");
            
            int r = Input.intVal("Enter 1-3: ");
            switch (r) {
                case 1: return Role.COLONY_RESIDENT;
                case 2: return Role.MISSION_CONTROL_OPERATOR;
                case 3: return Role.INFRASTRUCTURE_TECHNICIAN;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    private void doLogout() {
        if (!isLoggedIn()) { 
            System.out.println("No user is logged in."); 
            Input.pause(); 
            return; 
        }
        
        auth.logout(currentUser);
        System.out.println("Logged out: " + currentUser.username + 
                          " (User ID: " + currentUser.userId + ")");
        currentUser = null;
        Input.pause();
    }
    
    /**
     * Ensures user has active session before proceeding
     * @param refresh Whether to update last activity timestamp
     */
    private boolean ensureActiveSession() { 
        return ensureActiveSession(true); 
    }
    
    private boolean ensureActiveSession(boolean refresh) {
        if (!isLoggedIn()) { 
            System.out.println("Please login first (option 1)."); 
            Input.pause(); 
            return false; 
        }
        
        boolean expired = auth.isSessionExpired(currentUser, Instant.now());
        if (expired) { 
            System.out.println("Your session has expired. Please login again."); 
            Input.pause(); 
            return false; 
        }
        
        if (refresh) auth.touch(currentUser);
        return true;
    }
    
    /**
     * Displays user's schedule for a specific date
     */
    private void doViewSchedule() {
        if (!ensureActiveSession()) return;
        
        LocalDate date = Input.dateOptToday("Enter date (YYYY-MM-DD), blank for today: ");
        if (date == null) return;
        
        List<Task> tasks = schedule.getTasks(currentUser.username, date);
        System.out.println("\nTasks for " + date + ": " + tasks.size());
        
        if (tasks.isEmpty()) 
            System.out.println("(No tasks for this date)");
        else 
            for (Task t : tasks) System.out.println(" - " + t);
        
        Input.pause();
    }
    
    /**
     * Adds or updates a task in user's schedule
     */
    private void doAddOrUpdateTask() {
        if (!ensureActiveSession()) return;
        
        String id = Input.line("Task ID (e.g., t100): ");
        String title = Input.line("Title: ");
        int priority = Input.intRange("Priority (1=High .. 5=Low): ", 1, 5);
        LocalDate date = Input.dateReq("Date (YYYY-MM-DD): ");
        LocalTime time = Input.timeReq("Time (HH:MM 24h): ");
        
        Task task = new Task(id, title, priority, date, time);
        schedule.upsertTask(currentUser.username, task);
        System.out.println("Task saved/updated.");
        Input.pause();
    }
    
    private void showLastUpdate() {
        if (!ensureActiveSession()) return;
        
        Instant ts = schedule.getLastUpdate(currentUser.username);
        System.out.println("Last schedule update: " + ts);
        Input.pause();
    }
    
    /**
     * Simulates user inactivity for session timeout testing
     */
    private void simulateInactivity() {
        if (!ensureActiveSession(false)) return;
        
        int mins = Input.intVal("Simulate inactivity minutes: ");
        if (mins < 0) mins = 0;
        
        if (currentUser.lastActivity == null) 
            currentUser.lastActivity = Instant.now();
        
        currentUser.lastActivity = currentUser.lastActivity.minus(Duration.ofMinutes(mins));
        boolean expired = auth.isSessionExpired(currentUser, Instant.now());
        System.out.println("Session " + (expired ? "is EXPIRED." : "is still active."));
        Input.pause();
    }
    
    private void showAuditLog() {
        if (!ensureActiveSession()) return;
        
        List<String> log = auth.getAudit();
        if (log.isEmpty()) System.out.println("(Audit empty)");
        for (String line : log) System.out.println(line);
        Input.pause();
    }
    
    private void doBackup() {
        if (!ensureActiveSession()) return;
        
        Map<String, Map<LocalDate, List<Task>>> snap = schedule.snapshot();
        backup.backup(snap);
        System.out.println("Backup created. Users in snapshot: " + snap.keySet().size());
        Input.pause();
    }
    
    private void doRestore() {
        if (!ensureActiveSession()) return;
        
        Map<String, Map<LocalDate, List<Task>>> restored = backup.restore();
        schedule.restore(restored);
        System.out.println("Restore applied. Users restored: " + restored.keySet().size());
        Input.pause();
    }
    
    /**
     * Simulates system uptime for reliability reporting
     */
    private void simulateUptime() {
        if (!ensureActiveSession()) return;
        
        long hours = Input.longVal("Total hours to simulate (e.g., 24): ");
        long downtimeMinutes = Input.longVal("Downtime minutes (e.g., 1): ");
        
        long totalMs = hours * 60L * 60L * 1000L;
        long downMs = downtimeMinutes * 60L * 1000L;
        
        uptime.simulate(totalMs, downMs);
        double pct = uptime.uptimePercent();
        System.out.printf("Uptime: %.5f%%%n", pct);
        Input.pause();
    }
}