package com.app.incidentManagement.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.app.incidentManagement.dto.IncidentEvent;

@Service
public class IncidentEventService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void publishIncidentChanged(Long incidentId) {

        IncidentEvent event = new IncidentEvent(
                "INCIDENT_CHANGED",
                incidentId
        );

        messagingTemplate.convertAndSend(
                "/topic/incidents",
                event
        );

    }
}