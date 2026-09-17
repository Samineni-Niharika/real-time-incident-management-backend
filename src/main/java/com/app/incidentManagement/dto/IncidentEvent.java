package com.app.incidentManagement.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IncidentEvent {

    private String type;
    private Long incidentId;
}