package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.Environment;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeEnvironmentRequest {

    @NotNull(message = "Environment is required")
    private Environment environment;
}