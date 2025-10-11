package com.labndbnb.landbnb.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.labndbnb.landbnb.service.definition.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;

    public ImageServiceImpl(
            @Value("${cloud.name}") String name,
            @Value("${cloud.api.key}") String key,
            @Value("${cloud.api.secret}") String secret) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", name);
        config.put("api_key", key);
        config.put("api_secret", secret);
        this.cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws IOException {
        File file = convert(image);
        try {
            return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "landbnb"));
        } finally {
            if (file.exists() && !file.delete()) {
                log.warn("No se pudo eliminar el archivo temporal: {}", file.getAbsolutePath());
            }
        }
    }

    @Override
    public Map delete(String imageId) throws IOException {
        return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(image.getOriginalFilename(), null);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        }
        return file;
    }
}