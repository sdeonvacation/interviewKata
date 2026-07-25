package dev.interviewkata.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_returns404() {
        EntityNotFoundException ex = new EntityNotFoundException("Card not found: abc");

        ResponseEntity<Map<String, String>> result = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Card not found: abc", result.getBody().get("error"));
    }

    @Test
    void handleBadRequest_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid value");

        ResponseEntity<Map<String, String>> result = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid value", result.getBody().get("error"));
    }

    @Test
    void handleConflict_returns409() {
        IllegalStateException ex = new IllegalStateException("Card already graded in this session");

        ResponseEntity<Map<String, String>> result = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Card already graded in this session", result.getBody().get("error"));
    }

    @Test
    void handleNpe_returns400WithFieldMessage() {
        NullPointerException ex = new NullPointerException("something null");

        ResponseEntity<Map<String, String>> result = handler.handleNpe(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid request: missing required field", result.getBody().get("error"));
    }

    @Test
    void handleCast_returns400WithTypeMessage() {
        ClassCastException ex = new ClassCastException("String cannot be cast to Integer");

        ResponseEntity<Map<String, String>> result = handler.handleCast(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid request: wrong field type", result.getBody().get("error"));
    }

    @Test
    void handleUnreadable_returns400WithJsonMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("parse error", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<Map<String, String>> result = handler.handleUnreadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid JSON body", result.getBody().get("error"));
    }

    @Test
    void handleOptimisticLock_jpaException_returns409() {
        OptimisticLockException ex = new OptimisticLockException("concurrent update");

        ResponseEntity<Map<String, String>> result = handler.handleOptimisticLock(ex);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Concurrent modification detected, please retry", result.getBody().get("error"));
    }

    @Test
    void handleOptimisticLock_springException_returns409() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Card", UUID.randomUUID());

        ResponseEntity<Map<String, String>> result = handler.handleOptimisticLock(ex);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Concurrent modification detected, please retry", result.getBody().get("error"));
    }
}
