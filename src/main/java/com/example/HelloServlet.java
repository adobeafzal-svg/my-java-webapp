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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple servlet demonstrating modern Java web development.
 * 
 * KEY DIFFERENCES FROM OLD J2EE:
 * - @WebServlet annotation replaces web.xml servlet mapping
 * - Jakarta namespace (not javax) for Servlet 5.0+
 * - Uses try-with-resources for PrintWriter
 * 
 * This servlet responds to:
 *   GET /api/hello         - Returns JSON greeting
 *   GET /api/hello?name=X  - Returns personalized JSON greeting
 */
@WebServlet(urlPatterns = {"/api/hello"}, name = "HelloServlet")
public class HelloServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final GreetingService greetingService = new GreetingService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Get optional name parameter
        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            name = "World";
        }
        
        // Build response using our service
        String greeting = greetingService.createGreeting(name);
        
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("message", greeting);
        jsonResponse.addProperty("timestamp", LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        jsonResponse.addProperty("version", getApplicationVersion());
        
        // Write JSON response
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
    
    /**
     * Returns application version - useful for deployment verification
     */
    private String getApplicationVersion() {
        // In a real app, this might come from a properties file or manifest
        return "1.0.0-SNAPSHOT";
    }
}
