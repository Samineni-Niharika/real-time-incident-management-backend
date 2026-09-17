package com.app.incidentManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.incidentManagement.entity.IncidentAuditLog;

public interface IncidentAuditLogRepository extends JpaRepository<IncidentAuditLog, Long> {
	List<IncidentAuditLog> findByIncidentIdOrderByCreatedAtDesc(
            Long incidentId
    );
}
