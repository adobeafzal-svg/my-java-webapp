package com.example;

/**
 * Service class containing business logic.
 * 
 * WHY SEPARATE THIS FROM THE SERVLET?
 * - Easier to unit test (no servlet dependencies)
 * - Follows Single Responsibility Principle
 * - Can be reused by other servlets or services
 * - This pattern becomes essential as applications grow
 */
public class GreetingService {
    
    private static final String GREETING_TEMPLATE = "Hello, %s!";
    private static final int MAX_NAME_LENGTH = 100;
    
    /**
     * Creates a greeting message for the given name.
     * 
     * @param name The name to greet (will be sanitized)
     * @return A greeting message
     * @throws IllegalArgumentException if name is null
     */
    public String createGreeting(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        
        String sanitizedName = sanitizeName(name);
        return String.format(GREETING_TEMPLATE, sanitizedName);
    }
    
    /**
     * Sanitizes the input name by trimming whitespace and 
     * limiting length to prevent abuse.
     */
    String sanitizeName(String name) {
        String trimmed = name.trim();
        
        if (trimmed.isEmpty()) {
            return "World";
        }
        
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return trimmed.substring(0, MAX_NAME_LENGTH);
        }
        
        return trimmed;
    }
    
    /**
     * Validates if a name is acceptable.
     * Useful for form validation before processing.
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - no special characters that could cause issues
        return name.matches("^[a-zA-Z0-9\\s\\-']+$");
    }
}
