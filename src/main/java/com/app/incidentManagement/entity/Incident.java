package com.app.incidentManagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Incident{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String description;
	@Enumerated(EnumType.STRING)
	private Severity severity;
	@Enumerated(EnumType.STRING)
	private Priority priority;
	@Enumerated(EnumType.STRING)
	private IncidentStatus status;
	private String serviceName;
	@Enumerated(EnumType.STRING)
	private Environment environment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime resolvedAt;
	private String resolutionNotes;
	@ManyToOne
	@JoinColumn(name = "reporter_id")
	private User reporter;
	@ManyToOne
	@JoinColumn(name = "assigned_to")
	private User assignedTo;
	@ManyToOne
	@JoinColumn(name = "tester_id")
	private User tester;
	@Column(nullable = false)
	private boolean deleted = false;
	private LocalDateTime deletedAt;
	
	
	
	
	
}