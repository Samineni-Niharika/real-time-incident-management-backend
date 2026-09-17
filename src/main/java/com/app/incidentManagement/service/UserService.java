package com.app.incidentManagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.incidentManagement.dto.ChangePasswordRequest;
import com.app.incidentManagement.dto.ForgotPasswordRequest;
import com.app.incidentManagement.dto.LoginRequest;
import com.app.incidentManagement.dto.LoginResponse;
import com.app.incidentManagement.dto.RegisterRequest;
import com.app.incidentManagement.dto.ResetPasswordRequest;
import com.app.incidentManagement.dto.UserResponse;
import com.app.incidentManagement.entity.Role;
import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.exception.InvalidCredentialsException;
import com.app.incidentManagement.repository.UserRepository;
import com.app.incidentManagement.security.JwtService;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private EmailService emailService;
	public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidCredentialsException(
                    "Email already registered"
            );
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        //Default role
        user.setRole(Role.REPORTER);
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isActive()
        );
    }
	public LoginResponse loginUser(LoginRequest request) {

	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() ->
	                    new InvalidCredentialsException("Invalid email or password")
	            );

	    if (!user.isActive()) {
	    	throw new InvalidCredentialsException(
"User account is inactive"	    	);
	        
	    }

	    boolean passwordMatches =
	            passwordEncoder.matches(
	                    request.getPassword(),
	                    user.getPassword()
	            );

	    if (!passwordMatches) {
	        throw new InvalidCredentialsException(
	                "Invalid email or password"
	        );
	    }

	    String token =
	            jwtService.generateToken(user.getEmail());

	    return new LoginResponse(
	            token,
	            "Bearer",
	            user.getId(),
	            user.getName(),
	            user.getEmail(),
	            user.getRole(),
	            user.isActive()
	    );
	}
	public UserResponse updateUserRole(
	        Long userId,
	        Role newRole) {

	    User user = userRepository.findById(userId)
	            .orElseThrow(() ->
	                    new InvalidCredentialsException("User not found")
	            );

	    user.setRole(newRole);

	    User updatedUser = userRepository.save(user);

	    return new UserResponse(
	            updatedUser.getId(),
	            updatedUser.getName(),
	            updatedUser.getEmail(),
	            updatedUser.getRole(),
	            updatedUser.isActive()
	    );
	}
	public List<UserResponse> getAllDevelopers() {
	    return userRepository.findByRoleAndActiveTrue(Role.DEVELOPER)
	            .stream()
	            .map(user -> new UserResponse(
	                    user.getId(),
	                    user.getName(),
	                    user.getEmail(),
	                    user.getRole(),
	                    user.isActive()
	            ))
	            .toList();
	}
	public List<UserResponse> getAllTesters() {
	    return userRepository.findByRoleAndActiveTrue(Role.TESTER)
	            .stream()
	            .map(user -> new UserResponse(
	                    user.getId(),
	                    user.getName(),
	                    user.getEmail(),
	                    user.getRole(),
	                    user.isActive()
	            ))
	            .toList();
	}
	public List<UserResponse> getAllUsers() {
	    return userRepository.findAll()
	            .stream()
	            .map(user -> new UserResponse(
	                    user.getId(),
	                    user.getName(),
	                    user.getEmail(),
	                    user.getRole(),
	                    user.isActive()
	            ))
	            .toList();
	}
	public void changePassword(ChangePasswordRequest request) {

		Authentication authentication =
		        SecurityContextHolder.getContext().getAuthentication();

		User user = (User) authentication.getPrincipal();

	    if (!passwordEncoder.matches(
	            request.getCurrentPassword(),
	            user.getPassword())) {

	        throw new InvalidCredentialsException(
	                "Current password is incorrect"
	        );
	    }

	    if (!request.getNewPassword().equals(
	            request.getConfirmPassword())) {

	        throw new IllegalArgumentException(
	                "New password and confirm password do not match"
	        );
	    }

	    user.setPassword(
	            passwordEncoder.encode(request.getNewPassword())
	    );

	    userRepository.save(user);
	}
	public void forgotPassword(ForgotPasswordRequest request) {

	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() ->
	                    new InvalidCredentialsException(
	                            "If the email is registered, a password reset link will be sent."
	                    )
	            );

	    String resetToken = UUID.randomUUID().toString();

	    user.setResetToken(resetToken);
	    user.setResetTokenExpiry(
	            LocalDateTime.now().plusMinutes(15)
	    );

	    userRepository.save(user);

	    String resetLink =
	            "http://localhost:5173/reset-password?token="
	            + resetToken;

	    emailService.sendPasswordResetEmail(
	            user.getEmail(),
	            resetLink
	    );
	}
	public void resetPassword(ResetPasswordRequest request) {

	    User user = userRepository.findByResetToken(request.getToken())
	            .orElseThrow(() ->
	                    new InvalidCredentialsException(
	                            "Invalid or expired reset token"
	                    )
	            );

	    if (user.getResetTokenExpiry() == null ||
	            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

	        throw new InvalidCredentialsException(
	                "Invalid or expired reset token"
	        );
	    }

	    if (!request.getNewPassword().equals(
	            request.getConfirmPassword())) {

	        throw new IllegalArgumentException(
	                "New password and confirm password do not match"
	        );
	    }

	    user.setPassword(
	            passwordEncoder.encode(request.getNewPassword())
	    );

	    // Token can no longer be reused
	    user.setResetToken(null);
	    user.setResetTokenExpiry(null);

	    userRepository.save(user);
	}
}

