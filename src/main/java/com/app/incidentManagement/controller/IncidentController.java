package com.app.incidentManagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.incidentManagement.dto.AssignIncidentRequest;
import com.app.incidentManagement.dto.AssignTesterRequest;
import com.app.incidentManagement.dto.ChangeEnvironmentRequest;
import com.app.incidentManagement.dto.ChangeSeverityRequest;
import com.app.incidentManagement.dto.CreateIncidentRequest;
import com.app.incidentManagement.dto.IncidentAuditLogResponse;
import com.app.incidentManagement.dto.IncidentDashboardResponse;
import com.app.incidentManagement.dto.IncidentResponse;
import com.app.incidentManagement.dto.ResolveIncidentRequest;
import com.app.incidentManagement.dto.UpdateIncidentRequest;
import com.app.incidentManagement.dto.UpdateStatusRequest;
import com.app.incidentManagement.entity.Environment;
import com.app.incidentManagement.entity.IncidentStatus;
import com.app.incidentManagement.entity.Priority;
import com.app.incidentManagement.entity.Severity;
import com.app.incidentManagement.service.AuditLogService;
import com.app.incidentManagement.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
	
	@Autowired
	private IncidentService incidentService;
	@Autowired
	private AuditLogService auditLogService;

	    @PostMapping
	    public IncidentResponse createIncident(
	            @RequestBody CreateIncidentRequest incident) {
	        return incidentService.createIncident(incident);
	    }

	    
	    @GetMapping
	    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(

	            @RequestParam(defaultValue = "0")
	            int page,

	            @RequestParam(defaultValue = "10")
	            int size,

	            @RequestParam(defaultValue = "createdAt")
	            String sortBy,

	            @RequestParam(defaultValue = "desc")
	            String direction,

	            @RequestParam(required = false)
	            String search,

	            @RequestParam(required = false)
	            IncidentStatus status,

	            @RequestParam(required = false)
	            Severity severity,
	            @RequestParam(required = false)
	            Priority priority,
	            @RequestParam(required = false)
	            Environment environment) {

	        Page<IncidentResponse> incidents =
	                incidentService.getAllIncidents(
	                		page,
	                        size,
	                        sortBy,
	                        direction,
	                        search,
	                        status,
	                        severity,
	                        priority,
	                        environment
	                );

	        return ResponseEntity.ok(incidents);
	    }
	    @GetMapping("/dashboard")
	    public ResponseEntity<IncidentDashboardResponse>
	    getDashboardStatistics() {

	        return ResponseEntity.ok(
	                incidentService.getDashboardStatistics()
	        );
	    }
	    @GetMapping("/{incidentId}/history")
	    public ResponseEntity<List<IncidentAuditLogResponse>>
	    getIncidentHistory(
	            @PathVariable Long incidentId) {

	        return ResponseEntity.ok(
	                auditLogService.getIncidentHistory(
	                        incidentId
	                )
	        );
	    }
	    @GetMapping("/{id}")
	    public IncidentResponse getIncidentById(
	            @PathVariable Long id) {

	        return incidentService.getIncidentById(id);
	    }
	    @PutMapping("/{id}")
	    public ResponseEntity<IncidentResponse> updateIncident(
	            @PathVariable Long id,
	            @Valid @RequestBody UpdateIncidentRequest request) {

	        return ResponseEntity.ok(
	            incidentService.updateIncident(id, request)
	        );
	    }
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteIncident(
	            @PathVariable Long id) {

	        incidentService.deleteIncident(id);

	        return ResponseEntity.noContent().build();
	    }
	    @PutMapping("/{id}/assign")
	    public ResponseEntity<IncidentResponse> assignIncident(
	            @PathVariable Long id,
	            @RequestBody AssignIncidentRequest request) {

	        return ResponseEntity.ok(
	                incidentService.assignIncident(id, request)
	        );
	    }
	    @PutMapping("/{id}/assign-tester")
	    public ResponseEntity<IncidentResponse> assignTester(
	            @PathVariable Long id,
	            @Valid @RequestBody AssignTesterRequest request) {

	        return ResponseEntity.ok(
	            incidentService.assignTester(id, request)
	        );
	    }
	    @PutMapping("/{id}/status")
	    public ResponseEntity<IncidentResponse> updateStatus(
	            @PathVariable Long id,
	            @RequestBody UpdateStatusRequest request) {

	        return ResponseEntity.ok(
	                incidentService.updateStatus(id, request)
	        );
	    }
	    @PutMapping("/{id}/environment")
	    public ResponseEntity<IncidentResponse> changeEnvironment(
	            @PathVariable Long id,
	            @Valid @RequestBody ChangeEnvironmentRequest request) {

	        return ResponseEntity.ok(
	            incidentService.changeEnvironment(
	                id,
	                request
	            )
	        );
	    }
	    @PutMapping("/{id}/severity")
	    public ResponseEntity<IncidentResponse> updateSeverity(
	            @PathVariable Long id,
	            @RequestBody ChangeSeverityRequest request) {

	        return ResponseEntity.ok(
	                incidentService.changeSeverity(id, request)
	        );
	    }
	    @PutMapping("/{id}/resolve")
	    public ResponseEntity<IncidentResponse> resolveIncident(
	            @PathVariable Long id,
	            @RequestBody ResolveIncidentRequest request) {

	        return ResponseEntity.ok(
	                incidentService.resolveIncident(id, request)
	        );
	    }
	    @PutMapping("/{incidentId}/approve")
	    public ResponseEntity<IncidentResponse> approveIncident(
	            @PathVariable Long incidentId,
	            @RequestParam(required=false) String testingNotes) {

	        return ResponseEntity.ok(
	                incidentService.approveIncident(
	                        incidentId,
	                        testingNotes
	                )
	        );
	    }
	    @PutMapping("/{incidentId}/reject")
	    public ResponseEntity<IncidentResponse> rejectIncident(
	            @PathVariable Long incidentId,
	            @RequestParam(required=false) String testingNotes) {

	        return ResponseEntity.ok(
	                incidentService.rejectIncident(
	                        incidentId,
	                        testingNotes
	                )
	        );
	    }
	    
	    
}
