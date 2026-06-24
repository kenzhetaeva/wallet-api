package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.UpdateUserRequest;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.security.UserPrincipal;
import com.aidana.wallet_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getUser(principal.getUserId());
    }

    @PutMapping("/me")
    public UserResponse updateUser(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return userService.updateUser(request, principal.getUserId());
    }
}
