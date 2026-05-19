package com.digitalwallet.bnkai.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String message;

    private List<String> details = new ArrayList<>();

    private String path;

    public ApiErrorResponse(
            Integer status,
            String message,
            LocalDateTime timestamp
    ) {

        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.details = new ArrayList<>();
    }
}
