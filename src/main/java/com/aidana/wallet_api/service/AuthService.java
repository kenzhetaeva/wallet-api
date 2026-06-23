package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.LoginUserRequest;
import com.aidana.wallet_api.DTO.request.RefreshTokenRequest;
import com.aidana.wallet_api.DTO.request.RegisterUserRequest;
import com.aidana.wallet_api.DTO.response.AuthResponse;
import com.aidana.wallet_api.DTO.response.RefreshResponse;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.entity.RefreshToken;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.repository.RefreshTokenRepository;
import com.aidana.wallet_api.repository.UserRepository;
import com.aidana.wallet_api.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HashUtils hashUtils;

    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return new UserResponse(user);
    }

    public AuthResponse login(LoginUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User with this email doesn't exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String generatedRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashUtils.sha256(generatedRefreshToken));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS));

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                generatedRefreshToken
        );
    }

    public RefreshResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashUtils.sha256(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        Instant now = Instant.now();

        if (refreshToken.getRevokedAt() != null) {
            throw new BadCredentialsException("Refresh token revoked");
        }

        if (!refreshToken.getExpiresAt().isAfter(now)) {
            throw new BadCredentialsException("Refresh token expired");
        }

        String accessToken = jwtService.generateAccessToken(
            refreshToken.getUser()
        );

        return new RefreshResponse(accessToken);
    }
}
