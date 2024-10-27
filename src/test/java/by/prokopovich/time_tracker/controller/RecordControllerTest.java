package by.prokopovich.time_tracker.controller;

import by.prokopovich.time_tracker.controller.handler.GlobalExceptionHandler;
import by.prokopovich.time_tracker.dto.request.CreateRecordRequest;
import by.prokopovich.time_tracker.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class RecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecordService recordService;

    @InjectMocks
    private RecordController recordController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(recordController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser(username = "sofia@mail.ru", roles = "USER")
    void сreateRecord() throws Exception {
        CreateRecordRequest request = new CreateRecordRequest(1L, "Test Description", "26/10/2024 15:00:00", (byte) 2);

        mockMvc.perform(post("/api/v1/records/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/records/search-all"))
                .andExpect(status().isUnauthorized());
    }
}
