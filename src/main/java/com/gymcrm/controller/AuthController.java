package com.gymcrm.controller;

import com.gymcrm.service.AuthenticationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/api/login")
    public void login(@RequestParam(value = "username") String username,
                      @RequestParam(value = "password") String password) {
        authenticationService.authenticate(username, password);
    }

    @PutMapping("/api/login/password")
    public void changePassword(@RequestParam(value = "username") String username,
                               @RequestParam(value = "oldPassword") String oldPassword,
                               @RequestParam(value = "newPassword") String newPassword) {
        authenticationService.changePassword(username, oldPassword, newPassword);
    }
}