package com.fintech.exception;

import com.fintech.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleValidationException(MethodArgumentNotValidException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getBindingResult().getFieldError().getDefaultMessage());
        response.setData(null);

        return response;
    }

    // exception handler for account not found
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleAccountNotFound(AccountNotFoundException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(null);

        return response;
    }

    // exception handler for insufficient balance
    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleInsufficientBalance(InsufficientBalanceException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(null);

        return response;
    }

    // exception handler for fraud detected
    @ExceptionHandler(FraudDetectedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleFraudDetected(FraudDetectedException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(null);

        return response;
    }

    // exception handler for optimistic locking failure
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<String> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Concurrent transaction detected. Please retry.");
        response.setData(null);

        return response;
    }
}