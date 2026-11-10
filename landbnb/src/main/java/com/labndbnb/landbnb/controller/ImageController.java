package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.util_dto.ResponseDTO;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ImageService imageService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map>> upload(@RequestParam("file") MultipartFile image) throws IOException {
        if (image.isEmpty() || image.getSize() > MAX_SIZE_BYTES) {
            throw new ExceptionAlert("La imagen está vacía o supera los 5MB");
        }
        if (image.getContentType() == null || !ALLOWED_TYPES.contains(image.getContentType())) {
            throw new ExceptionAlert("Formato no permitido. Usa JPG, PNG o WEBP");
        }
        Map response = imageService.upload(image);
        return ResponseEntity.ok(new ResponseDTO<>(false, response));
    }

    // Solo hosts pueden eliminar imágenes (se usan en alojamientos)
    @DeleteMapping
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ResponseDTO<String>> delete(@RequestParam("id") String id) throws IOException {
        imageService.delete(id);
        return ResponseEntity.ok(new ResponseDTO<>(false, "Imagen eliminada exitosamente"));
    }
}
