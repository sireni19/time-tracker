package by.prokopovich.time_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(@NotNull String firstname,
                            @NotNull String lastname,
                            @Email String email,
                            @NotNull @Min(1) String password,
                            @NotNull  @Pattern(regexp = "ADMIN|USER", message = "Роль ADMIN или USER")String role) {
}
