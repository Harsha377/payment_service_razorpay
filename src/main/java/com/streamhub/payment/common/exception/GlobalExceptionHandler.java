package com.streamhub.payment.common.exception;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.streamhub.payment.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MSG_VALIDATION_FAILED = "Validation failed";
    private static final String MSG_INVALID_REQUEST = "Invalid request";
    private static final String MSG_MALFORMED_JSON = "Malformed JSON request";
    private static final String MSG_DUPLICATE = "Duplicate value";
    private static final String MSG_INVALID_REFERENCE = "Invalid reference";
    private static final String MSG_REQUIRED = "Missing required value";
    private static final String MSG_CONSTRAINT = "Constraint violation";
    private static final String MSG_DATA_INTEGRITY = "Data integrity violation";
    private static final String MSG_ENDPOINT_NOT_FOUND = "Endpoint not found";
    private static final String MSG_FORBIDDEN = "You don't have permission to perform this action";
    private static final String DEFAULT_FIELD = "request";

    // SQLState constants (PostgreSQL)
    private static final String SQL_UNIQUE_VIOLATION = "23505";
    private static final String SQL_FK_VIOLATION     = "23503";
    private static final String SQL_NOT_NULL         = "23502";

    private record ErrorMapping(HttpStatus status, String message) {}

    private static final ErrorMapping DUPLICATE =
            new ErrorMapping(HttpStatus.CONFLICT, MSG_DUPLICATE);

    private static final ErrorMapping INVALID_REFERENCE =
            new ErrorMapping(HttpStatus.UNPROCESSABLE_ENTITY, MSG_INVALID_REFERENCE);

    private static final ErrorMapping REQUIRED =
            new ErrorMapping(HttpStatus.UNPROCESSABLE_ENTITY, MSG_REQUIRED);

    private static final ErrorMapping GENERIC_CONSTRAINT =
            new ErrorMapping(HttpStatus.BAD_REQUEST, MSG_CONSTRAINT);

    private static final ErrorMapping GENERIC_DATA_INTEGRITY =
            new ErrorMapping(HttpStatus.BAD_REQUEST, MSG_DATA_INTEGRITY);

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           String path,
                                                           Map<String, String> errors
    ) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(status.value())
                .error(status.name())
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private String getPath(HttpServletRequest request) {
        return Optional.ofNullable(request.getRequestURI()).orElse("N/A");
    }

    // Body validation (DTO → @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        log.warn("Validation error at {}", request.getRequestURI());
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(
                                error.getDefaultMessage(),
                                "Invalid value"
                        ),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST, // 400 → client sent wrong data
                MSG_VALIDATION_FAILED,
                request.getRequestURI(),
                errors
        );

    }
    //    Param / Path validation (@PathVariable, @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleParamValidation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        v -> Objects.requireNonNullElse(
                                v.getMessage(),
                                "Invalid value"
                        ),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                MSG_VALIDATION_FAILED,
                request.getRequestURI(),
                errors
        );
    }

    //  Request format issues (type/missing params)
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            String type = Optional.ofNullable(mismatch.getRequiredType())
                    .map(Class::getSimpleName)
                    .orElse("unknown");

            errors.put(mismatch.getName(), "Invalid value. Expected type: " + type);

        } else if (ex instanceof MissingServletRequestParameterException missing) {
            errors.put(missing.getParameterName(), missing.getMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, MSG_INVALID_REQUEST, getPath(request), errors);
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(paramResult -> {

            String paramName = paramResult.getMethodParameter().getParameterName();

            paramResult.getResolvableErrors().forEach(error -> {

                String field = paramName;

                // If it's a field-level error (DTO / record / list element)
                if (error instanceof org.springframework.validation.FieldError fe) {
                    field = fe.getField();
                }

                errors.put(field,
                        Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"));
            });
        });

        log.warn("Handler method validation failed at {} : {}",
                request.getRequestURI(), errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                MSG_VALIDATION_FAILED,
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {

            String fieldName = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            if (fieldName.isEmpty()) {
                fieldName = DEFAULT_FIELD;
            }

            String rejectedValue = String.valueOf(ife.getValue());
            String targetType = ife.getTargetType().getSimpleName();

            errors.put(
                    fieldName,
                    "Invalid value '" + rejectedValue + "' for type " + targetType
            );

            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    MSG_VALIDATION_FAILED,
                    request.getRequestURI(),
                    errors
            );
        }

        // fallback → malformed JSON (syntax error, broken structure)
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                MSG_MALFORMED_JSON,
                request.getRequestURI(),
                Map.of(
                        DEFAULT_FIELD,
                        Objects.requireNonNullElse(
                                ex.getMostSpecificCause().getMessage(),
                                MSG_INVALID_REQUEST+" payload"
                        )
                )
        );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad request at {} : {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(DEFAULT_FIELD, Objects.requireNonNullElse(
                ex.getMessage(),
                MSG_INVALID_REQUEST
        ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                MSG_INVALID_REQUEST,
                request.getRequestURI(),
                errors
        );
    }
    private String extractSqlState(Throwable root) {
        if (root instanceof SQLException sqlEx) {
            return sqlEx.getSQLState();
        }
        return null;
    }
    private String extractField(String message) {
        if (message == null) return DEFAULT_FIELD;

        // PostgreSQL format: column "age"
        Pattern pattern = Pattern.compile("column \"(.*?)\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return DEFAULT_FIELD;
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        // Log full exception for debugging
        log.error(MSG_DATA_INTEGRITY+" at {}", request.getRequestURI(), ex);

        Map<String, String> errors = new LinkedHashMap<>();

        Throwable root = Optional.ofNullable(ex.getMostSpecificCause()).orElse(ex);

        String message = Optional.ofNullable(root.getMessage()).orElse("");
        String lower   = message.toLowerCase();

        String sqlState = extractSqlState(root);
        String field    = extractField(message);

        ErrorMapping mapping;

        // 1. SQLState (primary & reliable)
        if (SQL_UNIQUE_VIOLATION.equals(sqlState)) {
            mapping = DUPLICATE;
        }
        else if (SQL_FK_VIOLATION.equals(sqlState)) {
            mapping = INVALID_REFERENCE;
        }
        else if (SQL_NOT_NULL.equals(sqlState)) {
            mapping = REQUIRED;
        }

        // 2. Hibernate fallback
        else if (root instanceof org.hibernate.exception.ConstraintViolationException cve) {

            String constraint = Optional.ofNullable(cve.getConstraintName())
                    .orElse("")
                    .toLowerCase();

            String rootMessage = Optional.ofNullable(root.getMessage()).orElse("").toLowerCase();

            if (constraint.contains("unique")) {
                mapping = DUPLICATE;
            }
            else if (constraint.contains("fk")) {
                mapping = INVALID_REFERENCE;
            }
            else if (constraint.contains("not_null") || rootMessage.contains("not null")) {
                mapping = REQUIRED;
            }
            else {
                mapping = GENERIC_CONSTRAINT;
            }
        }

        // 3. Message fallback
        else if (lower.contains("duplicate") || lower.contains("unique")) {
            mapping = DUPLICATE;
        }
        else if (lower.contains("foreign key")) {
            mapping = INVALID_REFERENCE;
        }
        else if (lower.contains("not null")) {
            mapping = REQUIRED;
        }
        else {
            mapping = GENERIC_DATA_INTEGRITY;
        }

        errors.put(field, mapping.message());

        return buildResponse(
                mapping.status(),
                mapping.message(),
                request.getRequestURI(),
                errors
        );
    }


    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                MSG_ENDPOINT_NOT_FOUND,
                request.getRequestURI(),
                null
        );
    }
    //400,401,403,404,409,422
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.warn("API exception at {} : {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getErrors()
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(HttpServletRequest request,AccessDeniedException ex){
        ApiErrorResponse errorResponse=ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message(MSG_FORBIDDEN)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,  // 500 → server bug
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

}
