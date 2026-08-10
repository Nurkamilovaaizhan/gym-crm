package com.gymcrm.controller;

import com.gymcrm.dto.TokenDto;
import com.gymcrm.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public TokenDto login(@RequestParam("username") String username,
                          @RequestParam("password") String password) {
        String token = authenticationService.login(username, password);
        return new TokenDto(username, token);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        authenticationService.logout(authorizationHeader);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/login/password")
    public void changePassword(@RequestParam("username") String username,
                               @RequestParam("oldPassword") String oldPassword,
                               @RequestParam("newPassword") String newPassword) {
        authenticationService.changePassword(username, oldPassword, newPassword);
    }
}