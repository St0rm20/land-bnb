package com.labndbnb.landbnb.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labndbnb.landbnb.controller.CommentController;
import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.service.definition.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com.labndbnb.landbnb.security.*"))
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean//por fiiiiin
    private CommentService commentService;

    // Datos de prueba comunes
    private final Long MOCK_ID = 1L;
    private final String MOCK_USER_EMAIL = "test@user.com";

    // --- Definición de Mocks ---
    private ReviewRequest createMockReviewRequest() {
        return new ReviewRequest(3, 3, "Great place");
    }

    private CommentAnswerDto createMockAnswerDto() {
        return new CommentAnswerDto("thanks!", 2L);
    }

    @Test
    @DisplayName("POST /api/comments - Éxito")
    void testCreateComment_Success() throws Exception {
        // Arrange
        ReviewRequest request = createMockReviewRequest();
        InfoDto mockInfo = new InfoDto("Review created", "Review created");

        when(commentService.createComment(any(ReviewRequest.class), any()))
                .thenReturn(mockInfo);

        // Act & Assert
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review created"))
                .andExpect(jsonPath("$.details").value("Review created"));
    }
}