package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.LoginUserRequest;
import com.aidana.wallet_api.DTO.request.RefreshTokenRequest;
import com.aidana.wallet_api.DTO.request.RegisterUserRequest;
import com.aidana.wallet_api.DTO.response.AuthResponse;
import com.aidana.wallet_api.DTO.response.RefreshResponse;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.entity.RefreshToken;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Role;
import com.aidana.wallet_api.repository.RefreshTokenRepository;
import com.aidana.wallet_api.repository.UserRepository;
import com.aidana.wallet_api.util.HashUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private HashUtils hashUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldCreateUser() {

        RegisterUserRequest request = registerUserRequest();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("hashedPassword");

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User user = captor.getValue();
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertEquals("hashedPassword", user.getPassword());

        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {

        RegisterUserRequest request = registerUserRequest();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "User with this email already exists",
                exception.getMessage()
        );

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginUser() {

        LoginUserRequest request = loginUserRequest();

        User user = new User();
        user.setPassword("123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(true);
        when(hashUtils.sha256(anyString()))
                .thenReturn("hashedRefreshToken");
        when(jwtService.generateAccessToken(user))
                .thenReturn("accessToken");

        AuthResponse response = authService.login(request);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken refreshToken = captor.getValue();
        assertEquals(user, refreshToken.getUser());
        assertEquals("hashedRefreshToken", refreshToken.getTokenHash());
        assertNotNull(refreshToken.getCreatedAt());
        assertNotNull(refreshToken.getExpiresAt());
        assertNull(refreshToken.getRevokedAt());
        assertTrue(refreshToken.getExpiresAt().isAfter(refreshToken.getCreatedAt()));

        assertEquals("accessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(hashUtils).sha256(anyString());
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void shouldThrowWhenUserNotFound() {

        LoginUserRequest request = loginUserRequest();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "User with this email doesn't exist",
                exception.getMessage()
        );

        verify(userRepository).findByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(hashUtils);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowWhenPasswordIncorrect() {

        LoginUserRequest request = loginUserRequest();

        User user = new User();
        user.setPassword("456");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(hashUtils);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRefreshToken() {

        String hashedRefreshToken = "hashedRefreshToken";
        String accessToken = "accessToken";

        RefreshTokenRequest request = refreshTokenRequest();

        User user = new User();
        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(now.plus(1, ChronoUnit.DAYS));
        refreshToken.setRevokedAt(null);

        when(hashUtils.sha256(request.getRefreshToken()))
                .thenReturn(hashedRefreshToken);
        when(refreshTokenRepository.findByTokenHash(hashedRefreshToken))
                .thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(user))
                .thenReturn(accessToken);

        RefreshResponse response = authService.refreshToken(request);

        assertEquals(accessToken, response.getAccessToken());

        verify(hashUtils).sha256(request.getRefreshToken());
        verify(refreshTokenRepository).findByTokenHash(hashedRefreshToken);
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void shouldThrowWhenTokenNotFound() {

        String hashedRefreshToken = "hashedRefreshToken";

        RefreshTokenRequest request = refreshTokenRequest();

        when(hashUtils.sha256(request.getRefreshToken()))
                .thenReturn(hashedRefreshToken);
        when(refreshTokenRepository.findByTokenHash(hashedRefreshToken))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals(
                "Refresh token not found",
                exception.getMessage()
        );

        verify(hashUtils).sha256(request.getRefreshToken());
        verify(refreshTokenRepository).findByTokenHash(hashedRefreshToken);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowWhenRefreshTokenRevoked() {

        String hashedRefreshToken = "hashedRefreshToken";

        RefreshTokenRequest request = refreshTokenRequest();
        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevokedAt(now);

        when(hashUtils.sha256(request.getRefreshToken()))
                .thenReturn(hashedRefreshToken);
        when(refreshTokenRepository.findByTokenHash(hashedRefreshToken))
                .thenReturn(Optional.of(refreshToken));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals(
                "Refresh token revoked",
                exception.getMessage()
        );

        verify(hashUtils).sha256(request.getRefreshToken());
        verify(refreshTokenRepository).findByTokenHash(hashedRefreshToken);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowWhenRefreshTokenExpired() {

        String hashedRefreshToken = "hashedRefreshToken";

        RefreshTokenRequest request = refreshTokenRequest();
        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(now.minus(1, ChronoUnit.MINUTES));
        refreshToken.setRevokedAt(null);

        when(hashUtils.sha256(request.getRefreshToken()))
                .thenReturn(hashedRefreshToken);
        when(refreshTokenRepository.findByTokenHash(hashedRefreshToken))
                .thenReturn(Optional.of(refreshToken));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals(
                "Refresh token expired",
                exception.getMessage()
        );

        verify(hashUtils).sha256(request.getRefreshToken());
        verify(refreshTokenRepository).findByTokenHash(hashedRefreshToken);
        verifyNoInteractions(jwtService);
    }

    private RegisterUserRequest registerUserRequest() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("johndoe@example.com");
        request.setPassword("123");

        return request;
    }

    private LoginUserRequest loginUserRequest() {
        LoginUserRequest request = new LoginUserRequest();
        request.setEmail("johndoe@example.com");
        request.setPassword("123");

        return request;
    }

    private RefreshTokenRequest refreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        return request;
    }
}
