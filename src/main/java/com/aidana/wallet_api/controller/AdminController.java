package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.UpdateUserRequest;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PutMapping("/users/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(request, userId);
    }
}
