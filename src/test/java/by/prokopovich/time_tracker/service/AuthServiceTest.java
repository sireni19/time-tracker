package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.RefreshTokenRequest;
import by.prokopovich.time_tracker.dto.request.SignInRequest;
import by.prokopovich.time_tracker.dto.request.SignUpRequest;
import by.prokopovich.time_tracker.dto.response.JwtAuthenticationResponse;
import by.prokopovich.time_tracker.dto.response.UserSignUpResponse;
import by.prokopovich.time_tracker.entity.Role;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private SignInRequest signInRequest;
    private SignUpRequest signUpRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    public void setUp() {
        signInRequest = new SignInRequest("eva@mail.ru", "password");
        signUpRequest = new SignUpRequest("Dominik", "Storrenta", "dom4@gmail.com", "password", "ADMIN");
        refreshTokenRequest = new RefreshTokenRequest("refreshTokenExample");
    }

    @Test
    void signIn_shouldReturnJwtAuthenticationResponse() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Eva")
                .lastname("Vishnevskaya")
                .email("eva@mail.ru")
                .password("password")
                .role(Role.USER)
                .build();
        String token = "jwtToken";
        String refreshToken = "refreshToken";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(signInRequest.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(token);
        when(jwtService.generateRefreshToken(any(), eq(user))).thenReturn(refreshToken);

        JwtAuthenticationResponse response = authService.signIn(signInRequest);

        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(refreshToken, response.refreshToken());
    }

    @Test
    void signIn_shouldThrowExceptionWhenUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(signInRequest.email())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.signIn(signInRequest));
        assertEquals("Неправильные логин или пароль", exception.getMessage());
    }

    @Test
    void signUp_success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname(signUpRequest.firstname())
                .lastname(signUpRequest.lastname())
                .email(signUpRequest.email())
                .password(signUpRequest.password())
                .role(Role.ADMIN)
                .build();

        when(passwordEncoder.encode(signUpRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserSignUpResponse response = authService.signUp(signUpRequest);

        assertNotNull(response);
        assertEquals("Dominik", response.firstname());
        assertEquals("Storrenta", response.lastname());
        assertEquals("dom4@gmail.com", response.email());
        assertEquals("ADMIN", response.role());
    }

    @Test
    void signUp_userAlreadyExists() {
        when(userRepository.save(any(User.class))).thenThrow(ConstraintViolationException.class);

        assertThrows(ConstraintViolationException.class, () -> authService.signUp(signUpRequest));
        verify(passwordEncoder).encode(signUpRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
     void refreshToken_success() {
        String userEmail = "jane@gmail.com";
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Jane")
                .lastname("Fest")
                .email(userEmail)
                .build();

        String newToken = "newJwtToken";

        when(jwtService.extractUsername(refreshTokenRequest.token())).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshTokenRequest.token(), user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(newToken);

        JwtAuthenticationResponse response = authService.refreshToken(refreshTokenRequest);

        assertNotNull(response);
        assertEquals(newToken, response.token());
        assertEquals(refreshTokenRequest.token(), response.refreshToken());
    }

    @Test
     void refreshToken_shouldThrowNoSuchElementException() {
        String userEmail = "jane@gmail.com";

        when(jwtService.extractUsername(refreshTokenRequest.token())).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            authService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
     void refreshToken_tokenInvalid() {
        String userEmail = "jane@gmail.com";
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Jane")
                .lastname("Foster")
                .email(userEmail)
                .build();

        when(jwtService.extractUsername(refreshTokenRequest.token())).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshTokenRequest.token(), user)).thenReturn(false);

        JwtAuthenticationResponse response = authService.refreshToken(refreshTokenRequest);

        assertNull(response);
        verify(jwtService, never()).generateToken(user);
    }
}
