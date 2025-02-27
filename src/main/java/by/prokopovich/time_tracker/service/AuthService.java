package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.response.JwtAuthenticationResponse;
import by.prokopovich.time_tracker.dto.request.RefreshTokenRequest;
import by.prokopovich.time_tracker.dto.request.SignInRequest;
import by.prokopovich.time_tracker.dto.request.SignUpRequest;
import by.prokopovich.time_tracker.dto.response.UserSignUpResponse;
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

    public UserSignUpResponse signUp(SignUpRequest request) {
        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .build();
        User savedUser = userRepository.save(user);
        return UserSignUpResponse.builder()
                .id(savedUser.getId())
                .firstname(savedUser.getFirstname())
                .lastname(savedUser.getLastname())
                .email(savedUser.getEmail())
                .role(String.valueOf(savedUser.getRole()))
                .build();
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
