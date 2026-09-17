package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.IncidentStatus;
import com.app.incidentManagement.entity.Priority;
import com.app.incidentManagement.entity.Severity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateIncidentRequest {
	private String title;
	private String description;
	private Severity severity;
	private Priority priority;
	private IncidentStatus status;
	private String serviceName;
	private String resolutionNotes;
}
