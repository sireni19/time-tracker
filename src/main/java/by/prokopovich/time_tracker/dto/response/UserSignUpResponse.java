package by.prokopovich.time_tracker.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSignUpResponse(UUID id, String firstname, String lastname, String email, String role) {
}
