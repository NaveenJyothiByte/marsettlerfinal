import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Console input utilities with validation and error handling
 * Provides type-safe input methods with user-friendly prompts
 */
public class Input {
    
    private static final Scanner in = new Scanner(System.in);
    
    /** Reads a line of text with prompt */
    public static String line(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }
    
    /** Reads and validates integer input with retry on failure */
    public static int intVal(String prompt) {
        while (true) {
            try { 
                System.out.print(prompt); 
                return Integer.parseInt(in.nextLine().trim()); 
            } catch (Exception e) { 
                System.out.println("Enter a valid integer."); 
            }
        }
    }
    
    /** Reads and validates long input with retry on failure */
    public static long longVal(String prompt) {
        while (true) {
            try { 
                System.out.print(prompt); 
                return Long.parseLong(in.nextLine().trim()); 
            } catch (Exception e) { 
                System.out.println("Enter a valid number."); 
            }
        }
    }
    
    /** Reads integer within specified range with validation */
    public static int intRange(String prompt, int min, int max) {
        while (true) {
            int v = intVal(prompt);
            if (v >= min && v <= max) return v;
            System.out.println("Enter a value between " + min + " and " + max + ".");
        }
    }
    
    /** Reads optional date input - defaults to today if empty */
    public static LocalDate dateOptToday(String prompt) {
        String s = line(prompt);
        if (s.trim().isEmpty()) return LocalDate.now();
        try { 
            return LocalDate.parse(s.trim()); 
        } catch (DateTimeParseException e) { 
            System.out.println("Invalid date. Use YYYY-MM-DD."); 
            pause(); 
            return null; 
        }
    }
    
    /** Reads required date input with validation */
    public static LocalDate dateReq(String prompt) {
        while (true) {
            String s = line(prompt);
            try { 
                return LocalDate.parse(s.trim()); 
            } catch (DateTimeParseException e) { 
                System.out.println("Invalid date. Use YYYY-MM-DD."); 
            }
        }
    }
    
    /** Reads required time input with validation */
    public static LocalTime timeReq(String prompt) {
        while (true) {
            String s = line(prompt);
            try { 
                return LocalTime.parse(s.trim()); 
            } catch (DateTimeParseException e) { 
                System.out.println("Invalid time. Use HH:MM (24-hour)."); 
            }
        }
    }
    
    /** Pauses execution until user presses Enter */
    public static void pause() {
        System.out.print("Press Enter to continue...");
        in.nextLine();
    }
}