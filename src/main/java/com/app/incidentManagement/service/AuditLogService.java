package com.app.incidentManagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.app.incidentManagement.dto.IncidentAuditLogResponse;
import com.app.incidentManagement.dto.UserResponse;
import com.app.incidentManagement.entity.AuditAction;
import com.app.incidentManagement.entity.Incident;
import com.app.incidentManagement.entity.IncidentAuditLog;
import com.app.incidentManagement.entity.Role;
import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.exception.IncidentNotFoundException;
import com.app.incidentManagement.repository.IncidentAuditLogRepository;
import com.app.incidentManagement.repository.IncidentRepository;
@Service
public class AuditLogService {
	@Autowired
	private IncidentAuditLogRepository auditLogRepository;
	@Autowired
	private IncidentRepository incidentRepository;
    @PreAuthorize(
    	    "hasAnyRole('ADMIN','MANAGER','DEVELOPER','REPORTER','TESTER')"
    	)
    public List<IncidentAuditLogResponse> getIncidentHistory(
            Long incidentId) {
    	Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() ->
                        new IncidentNotFoundException(
                                "Incident not found with id: "
                                        + incidentId
                        ));

        User currentUser = (User)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        // Reporter can only view history of their own incidents
        if (currentUser.getRole() == Role.REPORTER &&
                !incident.getReporter()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to view this incident history"
            );
        }
        List<IncidentAuditLog> logs =
                auditLogRepository
                        .findByIncidentIdOrderByCreatedAtDesc(
                                incidentId
                        );

        return logs.stream()
                .map(this::convertToResponse)
                .toList();
    }
    private IncidentAuditLogResponse convertToResponse(
            IncidentAuditLog log) {

        IncidentAuditLogResponse response =
                new IncidentAuditLogResponse();

        response.setId(log.getId());
        response.setAction(log.getAction());

        response.setOldValue(
                log.getOldValue()
        );

        response.setNewValue(
                log.getNewValue()
        );

        response.setCreatedAt(
                log.getCreatedAt()
        );

        response.setPerformedBy(
                convertToUserResponse(
                        log.getPerformedBy()
                )
        );

        return response;
    }
    private UserResponse convertToUserResponse(
            User user) {

        if (user == null) {
            return null;
        }

        UserResponse response =
                new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());

        return response;
    }

    public void log(
            Incident incident,
            User user,
            AuditAction action,
            String oldValue,
            String newValue) {

        IncidentAuditLog log =
                new IncidentAuditLog();

        log.setIncident(incident);
        log.setPerformedBy(user);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
