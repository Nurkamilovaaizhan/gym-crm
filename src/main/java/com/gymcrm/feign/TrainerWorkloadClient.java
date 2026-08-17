package com.gymcrm.feign;

import com.gymcrm.dto.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gym-crm-workload")
public interface TrainerWorkloadClient {

    @PostMapping("/api/workload")
    void sendWorkload(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                      @RequestHeader("X-Transaction-Id") String transactionId,
                      @RequestBody TrainerWorkloadRequest request);
}