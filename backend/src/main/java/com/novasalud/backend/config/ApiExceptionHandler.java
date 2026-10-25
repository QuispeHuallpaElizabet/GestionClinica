package com.novasalud.backend.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validar(
    MethodArgumentNotValidException ex) {
    Map<String, String> errores = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
    errores.put(error.getField(), error.getDefaultMessage())
    );
    return ResponseEntity.badRequest().body(errores);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> error(RuntimeException ex) {
    return ResponseEntity.badRequest()
    .body(Map.of("mensaje", ex.getMessage()));
    }
}

