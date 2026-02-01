package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * Health check endpoint for CI/CD and container orchestration.
 * 
 * WHY THIS MATTERS FOR CI/CD:
 * - Kubernetes/Docker use this to verify the app is running
 * - Load balancers check this before routing traffic
 * - CI/CD pipelines call this to verify successful deployment
 * - Returns HTTP 200 = healthy, anything else = unhealthy
 * 
 * Common health check patterns:
 *   /health        - Basic "am I alive" check
 *   /health/live   - Liveness probe (is process running)
 *   /health/ready  - Readiness probe (can I accept traffic)
 */
@WebServlet(urlPatterns = {"/health", "/health/live", "/health/ready"}, name = "HealthCheckServlet")
public class HealthCheckServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String path = request.getServletPath();
        
        JsonObject health = new JsonObject();
        boolean isHealthy = true;
        
        // Basic health info
        health.addProperty("status", "UP");
        health.addProperty("application", "my-webapp");
        health.addProperty("version", "1.0.0-SNAPSHOT");
        
        // Add uptime info
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration uptime = Duration.ofMillis(uptimeMillis);
        health.addProperty("uptime", formatDuration(uptime));
        
        // For readiness checks, you might verify database connections, 
        // external service availability, etc.
        if ("/health/ready".equals(path)) {
            // In a real app, check database, cache, external services
            boolean dbHealthy = checkDatabaseConnection();
            health.addProperty("database", dbHealthy ? "UP" : "DOWN");
            isHealthy = isHealthy && dbHealthy;
        }
        
        // Set status based on health
        if (isHealthy) {
            response.setStatus(HttpServletResponse.SC_OK);
            health.addProperty("status", "UP");
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            health.addProperty("status", "DOWN");
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(health));
            out.flush();
        }
    }
    
    /**
     * Simulated database health check.
     * In a real application, this would attempt a database connection.
     */
    private boolean checkDatabaseConnection() {
        // Simulate a healthy database
        // Real implementation: try { dataSource.getConnection().isValid(5); }
        return true;
    }
    
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
