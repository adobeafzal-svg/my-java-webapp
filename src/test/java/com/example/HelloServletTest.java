package com.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HelloServlet using Mockito.
 * 
 * WHY MOCK SERVLET REQUEST/RESPONSE?
 * - No need to start a real server
 * - Tests run in milliseconds
 * - Can simulate any request scenario
 * - Isolated testing of servlet logic
 * 
 * MOCKITO BASICS:
 * - @Mock creates a fake object
 * - when(...).thenReturn(...) sets up behavior
 * - verify(...) confirms methods were called
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HelloServlet Tests")
class HelloServletTest {
    
    private HelloServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() throws Exception {
        servlet = new HelloServlet();
        
        // Set up a StringWriter to capture response output
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        // Mock the response.getWriter() to return our PrintWriter
        when(response.getWriter()).thenReturn(printWriter);
    }
    
    @Test
    @DisplayName("should return JSON greeting with default name")
    void shouldReturnDefaultGreeting() throws Exception {
        // Arrange - no name parameter
        when(request.getParameter("name")).thenReturn(null);
        
        // Act
        servlet.doGet(request, response);
        printWriter.flush();
        
        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Hello, World!"), 
                "Response should contain default greeting");
        assertTrue(output.contains("\"version\""), 
                "Response should contain version");
        assertTrue(output.contains("\"timestamp\""), 
                "Response should contain timestamp");
        
        // Verify content type was set
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
    }
    
    @Test
    @DisplayName("should return personalized greeting when name provided")
    void shouldReturnPersonalizedGreeting() throws Exception {
        // Arrange
        when(request.getParameter("name")).thenReturn("Alice");
        
        // Act
        servlet.doGet(request, response);
        printWriter.flush();
        
        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Hello, Alice!"), 
                "Response should contain personalized greeting");
    }
    
    @Test
    @DisplayName("should use default name for empty parameter")
    void shouldHandleEmptyName() throws Exception {
        // Arrange
        when(request.getParameter("name")).thenReturn("");
        
        // Act
        servlet.doGet(request, response);
        printWriter.flush();
        
        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Hello, World!"), 
                "Response should fall back to default greeting for empty name");
    }
    
    @Test
    @DisplayName("should use default name for whitespace-only parameter")
    void shouldHandleWhitespaceName() throws Exception {
        // Arrange
        when(request.getParameter("name")).thenReturn("   ");
        
        // Act
        servlet.doGet(request, response);
        printWriter.flush();
        
        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Hello, World!"), 
                "Response should fall back to default greeting for whitespace name");
    }
    
    @Test
    @DisplayName("response should be valid JSON")
    void shouldReturnValidJson() throws Exception {
        // Arrange
        when(request.getParameter("name")).thenReturn("Test");
        
        // Act
        servlet.doGet(request, response);
        printWriter.flush();
        
        // Assert
        String output = stringWriter.toString();
        
        // Basic JSON structure validation
        assertTrue(output.startsWith("{"), "Response should start with {");
        assertTrue(output.endsWith("}"), "Response should end with }");
        assertTrue(output.contains("\"message\""), "Response should have message field");
    }
}
