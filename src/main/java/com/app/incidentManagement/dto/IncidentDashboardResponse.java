package com.app.incidentManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IncidentDashboardResponse {
	private long totalIncidents;
    private long openIncidents;
    private long assignedIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long closedIncidents;
    private long criticalIncidents;
}
