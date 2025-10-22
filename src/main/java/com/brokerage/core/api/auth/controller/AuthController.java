package com.brokerage.core.api.auth.controller;

import com.brokerage.core.api.auth.service.AuthService;
import com.brokerage.core.base.response.BaseResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseResponse {

    private final AuthService authService;

    public AuthController(MessageSource messageSource, AuthService authService) {
        super(messageSource);
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam(required = false) String fullName) {
        var result = authService.register(username, password, fullName);
        return created("success.user.registered", result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        var result = authService.login(username, password);
        return ok("success.user.logged_in", result);
    }
}
