package shopping.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shopping.exceptions.ShopApiException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ShopApiException.class)
    public ProblemDetail handleShopApi(ShopApiException e){
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                e.getMessage()
        );
        detail.setTitle("Shop AI Failure");
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Request validation failed");

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                message
        );
        return detail;

    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e){
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage() == null ? "Unexpected server error": e.getMessage());

        detail.setTitle("Application failure");
        return detail;
    }
}
