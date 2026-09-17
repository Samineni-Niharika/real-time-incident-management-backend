package com.app.incidentManagement.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	private int status;
    private String message;
    private LocalDateTime timestamp;
}
