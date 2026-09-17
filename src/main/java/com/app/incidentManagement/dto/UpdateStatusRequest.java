package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.IncidentStatus;

public class UpdateStatusRequest {

    private IncidentStatus status;

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}