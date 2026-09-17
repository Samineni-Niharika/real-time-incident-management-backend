package com.app.incidentManagement.dto;

import com.app.incidentManagement.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
	 	private Long id;
	    private String name;
	    private String email;
	    private Role role;
	    private boolean active;
}
