package com.labndbnb.landbnb.unit;

import com.labndbnb.landbnb.controller.ImageController;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageController")
class ImageControllerTest {

    @Mock private ImageService imageService;
    @InjectMocks private ImageController controller;

    @Test
    @DisplayName("sube una imagen PNG válida")
    void uploadsValidImage() throws Exception {
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});
        when(imageService.upload(file)).thenReturn(Map.of("url", "http://x"));

        var res = controller.upload(file);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(imageService).upload(file);
    }

    @Test
    @DisplayName("rechaza tipos que no son imagen (p. ej. HTML/ejecutables)")
    void rejectsNonImage() throws Exception {
        var file = new MockMultipartFile("file", "x.html", "text/html", "<script>".getBytes());

        assertThatThrownBy(() -> controller.upload(file)).isInstanceOf(ExceptionAlert.class);
        verify(imageService, never()).upload(any());
    }

    @Test
    @DisplayName("rechaza archivos vacíos")
    void rejectsEmpty() throws Exception {
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> controller.upload(file)).isInstanceOf(ExceptionAlert.class);
    }

    @Test
    @DisplayName("rechaza archivos de más de 5MB")
    void rejectsTooLarge() throws Exception {
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[5 * 1024 * 1024 + 1]);

        assertThatThrownBy(() -> controller.upload(file)).isInstanceOf(ExceptionAlert.class);
        verify(imageService, never()).upload(any());
    }

    @Test
    @DisplayName("borrar imágenes está restringido al rol HOST y subir exige autenticación")
    void securityAnnotations() throws Exception {
        var delete = ImageController.class.getMethod("delete", String.class);
        var upload = ImageController.class.getMethod("upload", org.springframework.web.multipart.MultipartFile.class);

        assertThat(delete.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('HOST')");
        assertThat(upload.getAnnotation(PreAuthorize.class).value()).isEqualTo("isAuthenticated()");
    }
}
