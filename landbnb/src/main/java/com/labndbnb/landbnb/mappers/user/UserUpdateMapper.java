package com.labndbnb.landbnb.mappers.user;

import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserUpdateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    User toEntity(UserUpdateDto dto);

    @Mapping(target = "profilePictureUrl", source = "photoProfile")
    @Mapping(target = "bio", source = "description")
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);
}
