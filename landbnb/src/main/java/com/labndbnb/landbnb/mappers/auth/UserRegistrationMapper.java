package com.labndbnb.landbnb.mappers.auth;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "password")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "dateOfBirth", source = "birthDate")
    @Mapping(target = "role", expression = "java(UserRole.valueOf(dto.role()))")
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserRegistration dto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "dateOfBirth")
    UserRegistration toDto(User user);

}
