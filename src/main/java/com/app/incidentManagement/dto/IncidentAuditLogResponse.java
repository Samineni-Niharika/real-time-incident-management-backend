package com.app.incidentManagement.dto;

import java.time.LocalDateTime;

import com.app.incidentManagement.entity.AuditAction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncidentAuditLogResponse {
	 private Long id;
	    private AuditAction action;
	    private UserResponse performedBy;
	    private String oldValue;
	    private String newValue;
	    private LocalDateTime createdAt;

}
