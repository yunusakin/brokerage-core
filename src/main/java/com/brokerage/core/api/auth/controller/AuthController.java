package com.brokerage.core.api.auth.controller;

import com.brokerage.core.api.auth.dto.AuthRequest;
import com.brokerage.core.api.auth.service.AuthService;
import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.base.response.BaseResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest) {
        var result = authService.register(authRequest);
        return created(SuccessKeys.USER_REGISTERED, result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        var result = authService.login(authRequest);
        return ok(SuccessKeys.USER_LOGGED_IN, result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        var result = authService.refresh(refreshToken);
        return ok(SuccessKeys.USER_LOGGED_IN, result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ok(SuccessKeys.GENERIC_SUCCESS, null);
    }
}
