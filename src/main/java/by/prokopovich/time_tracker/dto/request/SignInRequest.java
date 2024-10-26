package by.prokopovich.time_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SignInRequest(@NotNull @Email String email,
                            @NotNull String password) {
}
