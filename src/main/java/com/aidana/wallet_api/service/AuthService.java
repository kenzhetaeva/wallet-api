package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.RegisterUserRequest;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        this.userRepository.save(user);

        return new UserResponse(user);
    }
}
