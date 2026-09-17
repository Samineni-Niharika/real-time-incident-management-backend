package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.Environment;
import com.app.incidentManagement.entity.Priority;
import com.app.incidentManagement.entity.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateIncidentRequest {
	@NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Environment is required")
    private Environment environment;

}
