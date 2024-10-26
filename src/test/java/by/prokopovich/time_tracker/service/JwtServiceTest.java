package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.entity.Role;
import by.prokopovich.time_tracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private String secret = "fgadaskdfaspodf40939lkjhff343923932dfdsf5698sdfdsf5234";
    private Duration jwtAccessLifetime;
    private Duration jwtRefreshLifetime;

    private User user;
    @Mock
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtAccessLifetime = Duration.ofMinutes(1);
        jwtRefreshLifetime = Duration.ofDays(10);
        user = new User();
        user.setEmail("semen_bakin@mail.ru");
        user.setId(UUID.randomUUID());
        user.setRole(Role.ADMIN);
        jwtService = new JwtService(secret, jwtAccessLifetime, jwtRefreshLifetime);
    }

    @Test
    void testGenerateToken_shouldGenerateToken() {
        UserDetails userDetails = user;

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
    }

    @Test
    void generateRefreshToken_shouldRefreshToken() {
        UserDetails userDetails = user;
        Map<String, Object> extraClaims = new HashMap<>();

        String refreshToken = jwtService.generateRefreshToken(extraClaims, userDetails);

        assertNotNull(refreshToken);
        assertTrue(refreshToken.startsWith("ey"));
    }

    @Test
    void extractUsername_shouldReturnUsername() {
        String token = jwtService.generateToken(user);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(user.getUsername(), extractedUsername);
    }

    @Test
    void isValidToken_shouldReturnTrue() {
        UserDetails userDetails = user;
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }
}
