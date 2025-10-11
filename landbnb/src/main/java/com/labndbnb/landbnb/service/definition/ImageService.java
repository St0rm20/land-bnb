package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ImageService {
    Map upload(MultipartFile image) throws IOException;
    Map delete(String imageId) throws IOException;
}