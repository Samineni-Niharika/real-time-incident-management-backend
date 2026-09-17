package com.app.incidentManagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.incidentManagement.entity.Role;
import com.app.incidentManagement.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    List<User> findByRoleAndActiveTrue(Role role);
    Optional<User> findByResetToken(String resetToken);
    
}
