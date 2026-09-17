package com.app.incidentManagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.incidentManagement.entity.Incident;
import com.app.incidentManagement.entity.IncidentStatus;
import com.app.incidentManagement.entity.Severity;

@Repository
public interface IncidentRepository extends JpaRepository<Incident,Long>, JpaSpecificationExecutor<Incident> {
	@Query("""
		    SELECT i FROM Incident i
		    WHERE LOWER(i.serviceName) LIKE LOWER(CONCAT('%', :search, '%'))
		       OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
		       OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
		""")
		Page<Incident> searchIncidents(
		        @Param("search") String search,
		        Pageable pageable);
	@Query("""
		    SELECT i FROM Incident i
		    WHERE i.status = :status
		""")
		Page<Incident> findByStatus(
		        @Param("status") IncidentStatus status,
		        Pageable pageable);
	long countByStatus(IncidentStatus status);

	long countBySeverity(Severity severity);
	List<Incident> findByReporterId(Long reporterId);
}
