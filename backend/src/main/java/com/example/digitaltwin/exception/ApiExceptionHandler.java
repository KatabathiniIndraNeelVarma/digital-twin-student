package com.example.digitaltwin.exception;

import com.example.digitaltwin.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,HttpServletRequest request){
        Map<String,String> fields=ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e->e.getField(),e->e.getDefaultMessage()==null?"Invalid value":e.getDefaultMessage(),(a,b)->a));
        return response(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","Request validation failed",request,fields);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> conflict(IllegalArgumentException ex,HttpServletRequest request){
        return response(HttpStatus.CONFLICT,"CONFLICT",ex.getMessage(),request,Map.of());
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> unauthorized(AuthenticationException ex,HttpServletRequest request){
        return response(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Email or password is incorrect",request,Map.of());
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException ex,HttpServletRequest request){
        HttpStatus code=HttpStatus.valueOf(ex.getStatusCode().value());
        return response(code,code.name(),ex.getReason()==null?code.getReasonPhrase():ex.getReason(),request,Map.of());
    }
    private ResponseEntity<ApiError> response(HttpStatus status,String code,String message,HttpServletRequest request,Map<String,String> fields){
        return ResponseEntity.status(status).body(new ApiError(Instant.now(),status.value(),code,message,request.getRequestURI(),fields));
    }
}
