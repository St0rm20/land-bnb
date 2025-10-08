package com.labndbnb.landbnb.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.labndbnb.landbnb.service.definition.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;

    @Value("${cloud.name}")
    private String name;

    @Value("${cloud.api.key}")
    private String key;

    @Value("${cloud.api.secret}")
    private String secret;

    public ImageServiceImpl(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", name);
        config.put("api_key", key);
        config.put("api_secret", secret);
        cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws Exception {
        File file = convert(image);
        return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "landbnb"));
    }

    @Override
    public Map delete(String imageId) throws Exception {
        return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(image.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(image.getBytes());
        fos.close();
        return file;
    }
}
