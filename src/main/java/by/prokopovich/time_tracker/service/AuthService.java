package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.JwtAuthenticationResponse;
import by.prokopovich.time_tracker.dto.RefreshTokenRequest;
import by.prokopovich.time_tracker.dto.SignInRequest;
import by.prokopovich.time_tracker.dto.SignUpRequest;
import by.prokopovich.time_tracker.entity.Role;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public User signUp(SignUpRequest request) {
        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .build();

        return userRepository.save(user);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Неправильные логин или пароль"));

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        return new JwtAuthenticationResponse(token, refreshToken);
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUsername(refreshTokenRequest.token());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow();

        if (jwtService.isTokenValid(refreshTokenRequest.token(), user)) {
            String token = jwtService.generateToken(user);
            return new JwtAuthenticationResponse(token, refreshTokenRequest.token());
        }

        return null;
    }
}
