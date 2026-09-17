package com.app.incidentManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.incidentManagement.dto.ChangePasswordRequest;
import com.app.incidentManagement.dto.LoginRequest;
import com.app.incidentManagement.dto.LoginResponse;
import com.app.incidentManagement.dto.RegisterRequest;
import com.app.incidentManagement.dto.UserResponse;
import com.app.incidentManagement.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private UserService userService;
	 @PostMapping("/register")
	    public ResponseEntity<UserResponse> register(
	            @Valid @RequestBody RegisterRequest request) {

	        UserResponse response =
	                userService.registerUser(request);

	        return ResponseEntity
	                .status(HttpStatus.CREATED)
	                .body(response);
	    }
	 @PostMapping("/login")
	 public ResponseEntity<LoginResponse> login(
	         @Valid @RequestBody LoginRequest request) {

	     LoginResponse response =
	             userService.loginUser(request);

	     return ResponseEntity.ok(response);
	 }
	 @PutMapping("/change-password")
	 public ResponseEntity<Void> changePassword(
	         @Valid @RequestBody ChangePasswordRequest request) {

	     userService.changePassword(request);

	     return ResponseEntity.ok().build();
	 }
}
