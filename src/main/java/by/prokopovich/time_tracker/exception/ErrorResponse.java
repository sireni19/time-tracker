package by.prokopovich.time_tracker.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String error;
    private String path;

    public ErrorResponse(String message, HttpStatus status, WebRequest request) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status.value();
        this.error = message;
        this.path = ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
