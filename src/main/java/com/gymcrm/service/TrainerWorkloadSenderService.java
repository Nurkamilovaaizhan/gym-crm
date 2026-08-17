package com.gymcrm.service;

import com.gymcrm.dto.TrainerWorkloadRequest;
import com.gymcrm.feign.TrainerWorkloadClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainerWorkloadSenderService {

    private final TrainerWorkloadClient trainerWorkloadClient;

    public TrainerWorkloadSenderService(TrainerWorkloadClient trainerWorkloadClient) {
        this.trainerWorkloadClient = trainerWorkloadClient;
    }

    public void send(String authorization, String transactionId, TrainerWorkloadRequest request) {
        trainerWorkloadClient.sendWorkload(authorization, transactionId, request);
    }
}