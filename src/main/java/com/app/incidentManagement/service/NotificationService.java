package com.app.incidentManagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.app.incidentManagement.dto.NotificationResponse;
import com.app.incidentManagement.entity.Incident;
import com.app.incidentManagement.entity.Notification;
import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private SimpUserRegistry simpUserRegistry;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    public void createNotification(
            User user,
            Incident incident,
            String message) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setIncident(incident);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification =
                notificationRepository.save(notification);
        simpUserRegistry.getUsers().forEach(
                simpUser -> System.out.println(
                        "USER = " + simpUser.getName()
                )
        );


        NotificationResponse response =
                convertToResponse(savedNotification);

        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                response
        );
    }
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
    private NotificationResponse convertToResponse(
            Notification notification) {

        NotificationResponse response =
                new NotificationResponse();

        response.setId(notification.getId());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }
    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to update this notification"
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }
}