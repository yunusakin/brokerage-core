package com.brokerage.core.api.auth.controller;

import com.brokerage.core.api.auth.service.AuthService;
import com.brokerage.core.base.constants.SuccessKeys;
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
        return created(SuccessKeys.USER_REGISTERED, result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        var result = authService.login(username, password);
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
