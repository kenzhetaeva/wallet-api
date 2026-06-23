package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.RegisterUserRequest;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public UserResponse registerUser(@RequestBody RegisterUserRequest request) {
        return authService.register(request);
    }
}
