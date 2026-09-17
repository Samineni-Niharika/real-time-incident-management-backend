package com.app.incidentManagement.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.app.incidentManagement.dto.AssignIncidentRequest;
import com.app.incidentManagement.dto.AssignTesterRequest;
import com.app.incidentManagement.dto.ChangeEnvironmentRequest;
import com.app.incidentManagement.dto.ChangeSeverityRequest;
import com.app.incidentManagement.dto.CreateIncidentRequest;
import com.app.incidentManagement.dto.IncidentDashboardResponse;
import com.app.incidentManagement.dto.IncidentResponse;
import com.app.incidentManagement.dto.ResolveIncidentRequest;
import com.app.incidentManagement.dto.UpdateIncidentRequest;
import com.app.incidentManagement.dto.UpdateStatusRequest;
import com.app.incidentManagement.dto.UserResponse;
import com.app.incidentManagement.entity.AuditAction;
import com.app.incidentManagement.entity.Environment;
import com.app.incidentManagement.entity.Incident;
import com.app.incidentManagement.entity.IncidentStatus;
import com.app.incidentManagement.entity.Priority;
import com.app.incidentManagement.entity.Role;
import com.app.incidentManagement.entity.Severity;
import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.exception.IncidentNotFoundException;
import com.app.incidentManagement.exception.InvalidStatusTransitionException;
import com.app.incidentManagement.repository.IncidentRepository;
import com.app.incidentManagement.repository.UserRepository;
import com.app.incidentManagement.specification.IncidentSpecification;

@Service
public class IncidentService {
	@Autowired
	private IncidentEventService incidentEventService;
	@Autowired
	private IncidentRepository incidentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuditLogService auditLogService;
	@Autowired
	private NotificationService notificationService;
	@PreAuthorize("hasAnyRole('REPORTER')")
	 public IncidentResponse createIncident(CreateIncidentRequest request)
	 {
		
		User currentUser = (User) SecurityContextHolder
		        .getContext()
		        .getAuthentication()
		        .getPrincipal();
		 Incident incident = new Incident();

		    incident.setTitle(request.getTitle());
		    incident.setDescription(request.getDescription());
		    incident.setSeverity(request.getSeverity());
		    incident.setPriority(request.getPriority());
		    incident.setServiceName(request.getServiceName());
		    incident.setEnvironment(request.getEnvironment());
		    incident.setReporter(currentUser);
		    incident.setStatus(IncidentStatus.OPEN);
		    incident.setCreatedAt(LocalDateTime.now());
		    incident.setUpdatedAt(LocalDateTime.now());
		    
		    Incident savedIncident =
		            incidentRepository.save(incident);
		    auditLogService.log(
		            savedIncident,
		            currentUser,
		            AuditAction.CREATED,
		            null,
		            "Incident created"
		    );
		    incidentEventService.publishIncidentChanged(incident.getId());
		    return convertToResponse(savedIncident);
	 }
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'REPORTER','TESTER')")
	 public IncidentResponse getIncidentById(Long id)
	 {
		  Incident incident = incidentRepository.findById(id)
		            .orElseThrow(() ->
		                new IncidentNotFoundException(
		                    "Incident not found with id: " + id
		                )
		            );
		  if (incident.isDeleted()) {
			    throw new IncidentNotFoundException(
			            "Incident not found with id: " + id
			    );
			}
		    User currentUser = (User) SecurityContextHolder
		            .getContext()
		            .getAuthentication()
		            .getPrincipal();
		    if (currentUser.getRole() == Role.REPORTER) {

		        if (incident.getReporter() == null ||
		                !incident.getReporter().getId()
		                        .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                    "You are not allowed to view this incident"
		            );
		        }
		    }

		    if (currentUser.getRole() == Role.DEVELOPER) {

		        if (incident.getAssignedTo() == null ||
		                !incident.getAssignedTo().getId()
		                        .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                    "You are not allowed to view this incident"
		            );
		        }
		    }

		    if (currentUser.getRole() == Role.TESTER) {

		        if (incident.getTester() == null ||
		                !incident.getTester().getId()
		                        .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                    "You are not allowed to view this incident"
		            );
		        }
		    }
		    
		    return convertToResponse(incident);
	 }
	@PreAuthorize("hasAnyRole('ADMIN')")
	public void deleteIncident(Long id) {

		User currentUser = (User) SecurityContextHolder
	            .getContext()
	            .getAuthentication()
	            .getPrincipal();
	    Incident incident = incidentRepository.findById(id)
	            .orElseThrow(() ->
	                    new IncidentNotFoundException(
	                            "Incident not found with id: " + id
	                    ));

	    if (incident.isDeleted()) {
	        throw new IllegalStateException(
	                "Incident is already deleted"
	        );
	    }

	    incident.setDeleted(true);
	    incident.setDeletedAt(LocalDateTime.now());

	    incidentRepository.save(incident);

	    auditLogService.log(
	            incident,
	            currentUser,
	            AuditAction.DELETED,
	            "deleted=false",
	            "deleted=true"
	    );

	    incidentEventService.publishIncidentChanged(
	            incident.getId()
	    );
	}
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
	 public IncidentResponse updateIncident(
		        Long id,
		        UpdateIncidentRequest request) {

		    Incident incident = incidentRepository.findById(id)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + id
		                    ));
		    User currentUser = (User) SecurityContextHolder
		            .getContext()
		            .getAuthentication()
		            .getPrincipal();

		    if (currentUser.getRole() == Role.DEVELOPER) {

		        if (incident.getAssignedTo() == null ||
		            !incident.getAssignedTo().getId()
		                    .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                "You can update only incidents assigned to you"
		            );
		        }
		    }
		    String oldTitle = incident.getTitle();
		    String oldDescription = incident.getDescription();
		    String oldPriority = incident.getPriority() != null
		            ? incident.getPriority().name()
		            : null;
		    String oldServiceName = incident.getServiceName();
		    String oldResolutionNotes = incident.getResolutionNotes();
		    if (request.getTitle() != null &&
		            !request.getTitle().equals(oldTitle)) {

		            incident.setTitle(request.getTitle());

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.UPDATED,
		                    oldTitle,
		                    request.getTitle()
		            );
		        }

		        // Update Description
		        if (request.getDescription() != null &&
		            !request.getDescription().equals(oldDescription)) {

		            incident.setDescription(request.getDescription());

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.UPDATED,
		                    oldDescription,
		                    request.getDescription()
		            );
		        }

		        // Update Severity
		        if (request.getSeverity() != null &&
		            request.getSeverity() != incident.getSeverity()) {

		            String oldSeverity = incident.getSeverity() != null
		                    ? incident.getSeverity().name()
		                    : null;

		            incident.setSeverity(request.getSeverity());

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.SEVERITY_CHANGED,
		                    oldSeverity,
		                    request.getSeverity().name()
		            );
		        }

		        // Update Priority
		        if (request.getPriority() != null &&
		            request.getPriority() != incident.getPriority()) {

		            String newPriority = request.getPriority().name();

		            incident.setPriority(request.getPriority());

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.UPDATED,
		                    oldPriority,
		                    newPriority
		            );
		        }

		        // Update Service Name
		        if (request.getServiceName() != null &&
		            !request.getServiceName().equals(oldServiceName)) {

		            incident.setServiceName(request.getServiceName());

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.UPDATED,
		                    oldServiceName,
		                    request.getServiceName()
		            );
		        }

		        // Update Resolution Notes
		        if (request.getResolutionNotes() != null &&
		            !request.getResolutionNotes().equals(oldResolutionNotes)) {

		            incident.setResolutionNotes(
		                    request.getResolutionNotes()
		            );

		            auditLogService.log(
		                    incident,
		                    currentUser,
		                    AuditAction.UPDATED,
		                    oldResolutionNotes,
		                    request.getResolutionNotes()
		            );
		        }

		    incident.setUpdatedAt(LocalDateTime.now());

		    Incident savedIncident =
		            incidentRepository.save(incident);
		    incidentEventService.publishIncidentChanged(incident.getId());
		    return convertToResponse(savedIncident);
		}
	 private IncidentResponse convertToResponse(
		        Incident incident) {

		    IncidentResponse response = new IncidentResponse();

		    response.setId(incident.getId());
		    response.setTitle(incident.getTitle());
		    response.setDescription(incident.getDescription());
		    response.setSeverity(incident.getSeverity());
		    response.setPriority(incident.getPriority());
		    response.setStatus(incident.getStatus());
		    response.setServiceName(incident.getServiceName());
		    response.setEnvironment(incident.getEnvironment());
		    response.setCreatedAt(incident.getCreatedAt());
		    response.setUpdatedAt(incident.getUpdatedAt());
		    response.setResolvedAt(incident.getResolvedAt());
		    response.setResolutionNotes(
		            incident.getResolutionNotes());
		    response.setReporter(
		    		convertToUserResponse( incident.getReporter())
		        );

		        response.setAssignedTo(
		        		convertToUserResponse( incident.getAssignedTo())
		        );
		        response.setTester(
		                convertToUserResponse(
		                    incident.getTester()
		                )
		            );
		    return response;
		}
	 private void validateStatusTransition(
		        Incident incident,
		        IncidentStatus next) {
		 IncidentStatus current = incident.getStatus();
		    if (current == IncidentStatus.OPEN
		            && next == IncidentStatus.ASSIGNED ) {
		    	 if (incident.getAssignedTo() == null) {
		             throw new InvalidStatusTransitionException(
		                 "Developer must be assigned before changing status to ASSIGNED"
		             );
		         }
		        return;
		    }

		    if (current == IncidentStatus.ASSIGNED
		            && next == IncidentStatus.IN_PROGRESS) {
		        return;
		    }


		    if (current == IncidentStatus.IN_PROGRESS
		            && next == IncidentStatus.RESOLVED) {
		        return;
		    }

		    if (current == IncidentStatus.RESOLVED
		            && next == IncidentStatus.TESTING) {

		        if (incident.getTester() == null) {
		            throw new InvalidStatusTransitionException(
		                "Tester must be assigned before starting testing"
		            );
		        }

		        return;
		    }
		    if (current == IncidentStatus.TESTING
		            && next == IncidentStatus.CLOSED) {
		        return;
		    }
		    if (current == IncidentStatus.REOPENED &&
		    	    next == IncidentStatus.ASSIGNED) {
		    	if (incident.getAssignedTo() == null) {
		            throw new InvalidStatusTransitionException(
		                "Developer must be assigned before changing status to ASSIGNED"
		            );
		        }
		    	return;
		    }
		    
		    throw new InvalidStatusTransitionException(
		            "Invalid status transition: "
		            + current + " → " + next
		    );
		}
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	 public IncidentResponse assignIncident(
		        Long incidentId,
		        AssignIncidentRequest request) {

		    Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));

		    User assignedUser = userRepository.findById(request.getAssignedTo())
		            .orElseThrow(() ->
		                    new RuntimeException(
		                            "User not found with id: " + request.getAssignedTo()
		                    ));
		    if (assignedUser.getRole() != Role.DEVELOPER) {
		        throw new RuntimeException(
		                "Only developers can be assigned incidents"
		        );
		    }
		    User previousUser = incident.getAssignedTo();
		    incident.setAssignedTo(assignedUser);

		    if (incident.getStatus() == IncidentStatus.OPEN ||
		            incident.getStatus() == IncidentStatus.REOPENED) {

		            incident.setStatus(
		                IncidentStatus.ASSIGNED
		            );
		        }
		    incident.setUpdatedAt(LocalDateTime.now());
		    User currentUser = (User)
		            SecurityContextHolder
		                    .getContext()
		                    .getAuthentication()
		                    .getPrincipal();
		    Incident savedIncident = incidentRepository.save(incident);
		    auditLogService.log(
		            savedIncident,
		            currentUser,
		            AuditAction.ASSIGNED,
		            previousUser != null
		                    ? previousUser.getEmail()
		                    : null,
		            assignedUser.getEmail()
		    );
		    notificationService.createNotification(
		            assignedUser,incident,
		             incident.getTitle() + " Incident has been assigned to you"
		    );
		    incidentEventService.publishIncidentChanged(incident.getId());
		    return convertToResponse(savedIncident);
		}
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	    public IncidentResponse assignTester(
	            Long incidentId,
	            AssignTesterRequest request) {

		 Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));

	        User tester = userRepository.findById(
	            request.getTesterId()
	        ).orElseThrow(() ->
	            new RuntimeException(
	                "User not found with id: " + request.getTesterId()
	            )
	        );

	        // Only TESTER users can be assigned as testers
	        if (tester.getRole() != Role.TESTER) {

	            throw new RuntimeException(
	                "Only testers can be assigned for testing"
	            );
	        }

	        /*
	         * Tester assignment happens only after
	         * developer has resolved the incident.
	         */
	        if (incident.getStatus() != IncidentStatus.RESOLVED) {

	            throw new InvalidStatusTransitionException(
	                "Tester can only be assigned to a RESOLVED incident"
	            );
	        }

	        User previousTester = incident.getTester();

	        incident.setTester(tester);

	        // RESOLVED -> TESTING
	        incident.setStatus(IncidentStatus.TESTING);

	        incident.setUpdatedAt(LocalDateTime.now());

	        User currentUser = (User)
		            SecurityContextHolder
		                    .getContext()
		                    .getAuthentication()
		                    .getPrincipal();

	        Incident savedIncident = incidentRepository.save(incident);

	        auditLogService.log(
	            savedIncident,
	            currentUser,
	            AuditAction.TESTER_ASSIGNED,
	            previousTester != null
	                ? previousTester.getEmail()
	                : null,
	            tester.getEmail()
	        );
	        notificationService.createNotification(
	                tester,incident,
	               incident.getTitle() + " Incident has been assigned to you for testing"
	        );
	        incidentEventService.publishIncidentChanged(incident.getId());
	        return convertToResponse(savedIncident);
	    }


	    // =========================================================
	    // CHANGE ENVIRONMENT
	    // =========================================================

	    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	    public IncidentResponse changeEnvironment(
	            Long incidentId,
	            ChangeEnvironmentRequest request) {

	    	Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));

	        Environment oldEnvironment = incident.getEnvironment();
	        Environment newEnvironment = request.getEnvironment();

	        if (newEnvironment == oldEnvironment) {

	            return convertToResponse(incident);
	        }

	        incident.setEnvironment(newEnvironment);
	        incident.setUpdatedAt(LocalDateTime.now());

	        User currentUser = (User)
		            SecurityContextHolder
		                    .getContext()
		                    .getAuthentication()
		                    .getPrincipal();

	        Incident savedIncident = incidentRepository.save(incident);

	        auditLogService.log(
	            savedIncident,
	            currentUser,
	            AuditAction.ENVIRONMENT_CHANGED,
	            oldEnvironment != null
	                ? oldEnvironment.name()
	                : null,
	            newEnvironment.name()
	        );
	        incidentEventService.publishIncidentChanged(incident.getId());
	        return convertToResponse(savedIncident);
	    }
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
	 public IncidentResponse updateStatus(
		        Long incidentId,
		        UpdateStatusRequest request) {

		    Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));
		    User currentUser = (User) SecurityContextHolder
		            .getContext()
		            .getAuthentication()
		            .getPrincipal();
		    if (currentUser.getRole() == Role.DEVELOPER) {

		        if (incident.getAssignedTo() == null ||
		            !incident.getAssignedTo().getId()
		                    .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                "You can update only incidents assigned to you"
		            );
		        }
		    }
		    IncidentStatus currentStatus = incident.getStatus();
		    IncidentStatus newStatus = request.getStatus();

		    validateStatusTransition(
		            incident,
		            newStatus
		    );

		    incident.setStatus(newStatus);
		    incident.setUpdatedAt(LocalDateTime.now());
		    if (newStatus == IncidentStatus.RESOLVED) {
		        incident.setResolvedAt(LocalDateTime.now());
		    }
		    Incident savedIncident =
		            incidentRepository.save(incident);
		    auditLogService.log(
		            incident,
		            currentUser,
		            AuditAction.STATUS_CHANGED,
		            currentStatus.name(),
		            newStatus.name()
		    );
		    incidentEventService.publishIncidentChanged(incident.getId());
		    return convertToResponse(savedIncident);
		}
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	 public IncidentResponse changeSeverity(
		        Long incidentId,
		        ChangeSeverityRequest request) {

		    Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));

		    Severity oldSeverity =
		            incident.getSeverity();

		    Severity newSeverity =
		            request.getSeverity();
		    if (newSeverity == oldSeverity) {
		        return convertToResponse(incident);
		    }

		    incident.setSeverity(newSeverity);
		    incident.setUpdatedAt(LocalDateTime.now());
		    User currentUser = (User)
		            SecurityContextHolder
		                    .getContext()
		                    .getAuthentication()
		                    .getPrincipal();
		    Incident savedIncident =
		            incidentRepository.save(incident);
		    auditLogService.log(
		            savedIncident,
		            currentUser,
		            AuditAction.SEVERITY_CHANGED,
		            oldSeverity != null
		                    ? oldSeverity.name()
		                    : null,
		            newSeverity.name()
		    );
		    incidentEventService.publishIncidentChanged(incident.getId());
		    return convertToResponse(savedIncident);
		}
	 @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
	 public IncidentResponse resolveIncident(
		        Long incidentId,
		        ResolveIncidentRequest request) {

		    Incident incident = incidentRepository.findById(incidentId)
		            .orElseThrow(() ->
		                    new IncidentNotFoundException(
		                            "Incident not found with id: " + incidentId
		                    ));
		    User currentUser = (User) SecurityContextHolder
		            .getContext()
		            .getAuthentication()
		            .getPrincipal();

		    if (currentUser.getRole() == Role.DEVELOPER) {

		        if (incident.getAssignedTo() == null ||
		            !incident.getAssignedTo().getId()
		                    .equals(currentUser.getId())) {

		            throw new AccessDeniedException(
		                "You can resolve only incidents assigned to you"
		            );
		        }
		    }
		    if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {

		        throw new InvalidStatusTransitionException(
		                "Incident can only be resolved when it is IN_PROGRESS"
		        );
		    }
		    IncidentStatus oldStatus =
		            incident.getStatus();
		    incident.setStatus(IncidentStatus.RESOLVED);

		    incident.setResolutionNotes(
		            request.getResolutionNotes()
		    );

		    incident.setResolvedAt(
		            LocalDateTime.now()
		    );

		    incident.setUpdatedAt(
		            LocalDateTime.now()
		    );
		    Incident savedIncident =
            incidentRepository.save(incident);
		    auditLogService.log(
		            savedIncident,
		            currentUser,
		            AuditAction.RESOLVED,
		            oldStatus.name(),
		            IncidentStatus.RESOLVED.name()
		    );
		    notificationService.createNotification(
		            incident.getReporter(),incident,
		            incident.getTitle() +" Incident has been resolved"
		    );
		    incidentEventService.publishIncidentChanged(incident.getId());
    return convertToResponse(savedIncident);
		}
	 @PreAuthorize(
		        "hasAnyRole('ADMIN', 'MANAGER', 'TESTER')"
		    )
		    public IncidentResponse approveIncident(
		            Long incidentId,
		            String testingNotes) {
		 User currentUser = (User)
			        SecurityContextHolder
			                .getContext()
			                .getAuthentication()
			                .getPrincipal();
		        Incident incident = incidentRepository.findById(
		                incidentId
		        )
		        .orElseThrow(() ->
		            new IncidentNotFoundException(
		                "Incident not found with id: "
		                + incidentId
		            )
		        );
		        if (currentUser.getRole() == Role.TESTER) {

		        	 if (incident.getTester() == null ||
		                     !incident.getTester()
		                         .getId()
		                         .equals(currentUser.getId())) {

		                     throw new AccessDeniedException(
		                         "You can review only incidents assigned to you"
		                     );
		                 }
			    }

		       
		        if (incident.getStatus() != IncidentStatus.TESTING) {

		            throw new InvalidStatusTransitionException(
		                "Only TESTING incidents can be approved"
		            );
		        }

		        incident.setStatus(
		            IncidentStatus.CLOSED
		        );

		        String existingNotes = incident.getResolutionNotes();

		        incident.setResolutionNotes(
		            (existingNotes != null ? existingNotes : "")
		            + "\n\nTesting Notes: "
		            + (testingNotes != null ? testingNotes : "")
		        );

		        incident.setUpdatedAt(
		            LocalDateTime.now()
		        );

		        Incident savedIncident =
			            incidentRepository.save(incident);
		        auditLogService.log(
		                savedIncident,
		                currentUser,
		                AuditAction.APPROVED,
		                IncidentStatus.TESTING.name(),
		                IncidentStatus.CLOSED.name()
		        );
		        notificationService.createNotification(
			            incident.getReporter(),incident,
			            incident.getTitle() +" Incident has been approved and closed"
			    );
		        incidentEventService.publishIncidentChanged(incident.getId());
			    return convertToResponse(savedIncident);
		    }

		    @PreAuthorize(
		        "hasAnyRole('ADMIN', 'MANAGER', 'TESTER')"
		    )
		    public IncidentResponse rejectIncident(
		            Long incidentId,
		            String testingNotes) {

		    	User currentUser = (User)
		    	        SecurityContextHolder
		    	                .getContext()
		    	                .getAuthentication()
		    	                .getPrincipal();
		        Incident incident = incidentRepository.findById(
		                incidentId
		        )
		        .orElseThrow(() ->
		            new IncidentNotFoundException(
		                "Incident not found with id: "
		                + incidentId
		            )
		        );
		        if (currentUser.getRole() == Role.TESTER) {

		            if (incident.getTester() == null ||
		                !incident.getTester()
		                    .getId()
		                    .equals(currentUser.getId())) {

		                throw new AccessDeniedException(
		                    "You can review only incidents assigned to you"
		                );
		            }
		        }

		        if (incident.getStatus() != IncidentStatus.TESTING) {

		            throw new InvalidStatusTransitionException(
		                "Only TESTING incidents can be rejected"
		            );
		        }

		        incident.setStatus(
		            IncidentStatus.REOPENED
		        );

		        String existingNotes = incident.getResolutionNotes();

		        incident.setResolutionNotes(
		            (existingNotes != null ? existingNotes : "")
		            + "\n\nTesting Notes: "
		            + (testingNotes != null ? testingNotes : "")
		        );

		        incident.setUpdatedAt(
		            LocalDateTime.now()
		        );

		        Incident savedIncident =
			            incidentRepository.save(incident);
		        auditLogService.log(
		                savedIncident,
		                currentUser,
		                AuditAction.REJECTED,
		                IncidentStatus.TESTING.name(),
		                IncidentStatus.REOPENED.name()
		        );
		        notificationService.createNotification(
			            incident.getReporter(),incident,
			            incident.getTitle() +" was rejected during testing and has been reopened"
			    );
		        incidentEventService.publishIncidentChanged(incident.getId());
			    return convertToResponse(savedIncident);
		    }
		    private UserResponse convertToUserResponse(User user) {

		        if (user == null) {
		            return null;
		        }

		        UserResponse response = new UserResponse();

		        response.setId(user.getId());
		        response.setName(user.getName());
		        response.setEmail(user.getEmail());
		        response.setRole(user.getRole());
		        response.setActive(user.isActive());
		        return response;
		    }
			@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','DEVELOPER','TESTER','REPORTER')")
		    public Page<IncidentResponse> getAllIncidents(
		    		int page,
		            int size,
		            String sortBy,
		            String direction,
		            String search,
		            IncidentStatus status,
		            Severity severity,
		            Priority priority,
		            Environment environment) {

		        Sort.Direction sortDirection =
		                direction.equalsIgnoreCase("asc")
		                        ? Sort.Direction.ASC
		                        : Sort.Direction.DESC;

		        Pageable pageable =
		                PageRequest.of(
		                        page,
		                        size,
		                        Sort.by(sortDirection, sortBy)
		                );

		        Specification<Incident> specification =
		                IncidentSpecification.filterIncidents(
		                        search,
		                        status,
		                        severity,
		                        priority,
		                        environment
		                );
		        Authentication authentication =
		                SecurityContextHolder.getContext().getAuthentication();

		        User user = (User) authentication.getPrincipal();

		        if (user.getRole() == Role.REPORTER) {
		            specification = specification.and(
		                    (root, query, cb) ->
		                            cb.equal(root.get("reporter").get("id"), user.getId())
		            );
		        }
		        else if (user.getRole() == Role.DEVELOPER) {

		            specification = specification.and(
		                (root, query, cb) ->
		                    cb.equal(
		                        root.get("assignedTo").get("id"),
		                        user.getId()
		                    )
		            );

		        } else if (user.getRole() == Role.TESTER) {

		            specification = specification.and(
		                (root, query, cb) ->
		                    cb.equal(
		                        root.get("tester").get("id"),
		                        user.getId()
		                    )
		            );
		        }
		        Page<Incident> incidentPage =
		                incidentRepository.findAll(
		                        specification,
		                        pageable
		                );
		        return incidentPage.map(this::convertToResponse);
		    }
			public IncidentDashboardResponse getDashboardStatistics() {

			    Authentication authentication =
			            SecurityContextHolder.getContext().getAuthentication();

			    User user = (User) authentication.getPrincipal();

			    Specification<Incident> specification =
			            (root, query, cb) -> cb.conjunction();

			    // REPORTER → only incidents created by them
			    if (user.getRole() == Role.REPORTER) {

			        specification = specification.and(
			                (root, query, cb) ->
			                        cb.equal(
			                                root.get("reporter").get("id"),
			                                user.getId()
			                        )
			        );

			    // DEVELOPER → only incidents assigned to them
			    } else if (user.getRole() == Role.DEVELOPER) {

			        specification = specification.and(
			                (root, query, cb) ->
			                        cb.equal(
			                                root.get("assignedTo").get("id"),
			                                user.getId()
			                        )
			        );

			    // TESTER → only incidents assigned to them
			    } else if (user.getRole() == Role.TESTER) {

			        specification = specification.and(
			                (root, query, cb) ->
			                        cb.equal(
			                                root.get("tester").get("id"),
			                                user.getId()
			                        )
			        );
			    }

			    long totalIncidents =
			            incidentRepository.count(specification);

			    long openIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("status"),
			                                            IncidentStatus.OPEN
			                                    )
			                    )
			            );

			    long assignedIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("status"),
			                                            IncidentStatus.ASSIGNED
			                                    )
			                    )
			            );

			    long inProgressIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("status"),
			                                            IncidentStatus.IN_PROGRESS
			                                    )
			                    )
			            );

			    long resolvedIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("status"),
			                                            IncidentStatus.RESOLVED
			                                    )
			                    )
			            );

			    long closedIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("status"),
			                                            IncidentStatus.CLOSED
			                                    )
			                    )
			            );

			    long criticalIncidents =
			            incidentRepository.count(
			                    specification.and(
			                            (root, query, cb) ->
			                                    cb.equal(
			                                            root.get("priority"),
			                                            Priority.P1
			                                    )
			                    )
			            );

			    return new IncidentDashboardResponse(
			            totalIncidents,
			            openIncidents,
			            assignedIncidents,
			            inProgressIncidents,
			            resolvedIncidents,
			            closedIncidents,
			            criticalIncidents
			    );
			}
 
}
