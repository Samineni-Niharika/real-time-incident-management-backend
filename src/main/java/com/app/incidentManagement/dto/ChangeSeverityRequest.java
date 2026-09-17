package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.Severity;

public class ChangeSeverityRequest {
	private Severity severity;

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
}
