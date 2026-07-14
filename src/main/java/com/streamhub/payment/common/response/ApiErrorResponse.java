package com.streamhub.payment.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {
    private int status;              // 400
    private String error;            // BAD_REQUEST
    private String message;          // High level message
    private String path;             // /auth/register
    private Instant timestamp;       // ISO-8601
    private Map<String, String> errors; // field → issue

}
