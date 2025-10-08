package com.labndbnb.landbnb.mappers.util;

import com.labndbnb.landbnb.dto.util_dto.ImageDto;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImageDtoMapper {

    // De ImageDto → String
    @Named("imageDtoToString")
    default String imageDtoToString(ImageDto imageDto) {
        return imageDto != null ? imageDto.url() : null;
    }

    // De String → ImageDto
    @Named("stringToImageDto")
    default ImageDto stringToImageDto(String imageUrl) {
        return imageUrl != null ? new ImageDto(imageUrl) : null;
    }

    // ✅ Añadimos @Named aquí
    @Named("mapImageDtoListToStringList")
    default List<String> mapImageDtoListToStringList(List<ImageDto> images) {
        return images != null
                ? images.stream().map(this::imageDtoToString).collect(Collectors.toList())
                : null;
    }

    @Named("mapStringListToImageDtoList")
    default List<ImageDto> mapStringListToImageDtoList(List<String> images) {
        return images != null
                ? images.stream().map(this::stringToImageDto).collect(Collectors.toList())
                : null;
    }
}
