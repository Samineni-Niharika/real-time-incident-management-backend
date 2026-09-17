package com.app.incidentManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(
            String to,
            String resetLink) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Password Reset - Incident Management Platform");

        message.setText(
                "Hello,\n\n"
                + "You requested to reset your password.\n\n"
                + "Click the link below to reset your password:\n"
                + resetLink
                + "\n\n"
                + "This link will expire in 15 minutes.\n\n"
                + "If you did not request this, you can safely ignore this email.\n\n"
                + "Regards,\n"
                + "Incident Management Platform"
        );

        mailSender.send(message);
    }
    @PostConstruct
    public void checkMailConfig() {
        String username = System.getenv("MAIL_USERNAME");
        String password = System.getenv("MAIL_PASSWORD");

        System.out.println("MAIL_USERNAME present: "
                + (username != null && !username.isBlank()));

        System.out.println("MAIL_USERNAME value: "
                + username);

        System.out.println("MAIL_PASSWORD present: "
                + (password != null && !password.isBlank()));

        System.out.println("MAIL_PASSWORD length: "
                + (password == null ? 0 : password.length()));
    }
}