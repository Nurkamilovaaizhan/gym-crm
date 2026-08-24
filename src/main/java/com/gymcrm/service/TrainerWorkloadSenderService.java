package com.gymcrm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.dto.TrainerWorkloadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainerWorkloadSenderService {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.jms.workload-queue}")
    private String workloadQueue;

    public TrainerWorkloadSenderService(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(String transactionId, TrainerWorkloadRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);

            jmsTemplate.convertAndSend(workloadQueue, payload, message -> {
                if (transactionId != null && !transactionId.isBlank()) {
                    message.setStringProperty("X-Transaction-Id", transactionId);
                }
                return message;
            });

            log.info("Workload message sent to queue={}, trainer={}, action={}",
                    workloadQueue, request.getTrainerUsername(), request.getActionType());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize workload message", ex);
        }
    }
}