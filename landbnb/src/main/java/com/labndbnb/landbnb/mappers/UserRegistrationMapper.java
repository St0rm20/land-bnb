package com.labndbnb.landbnb.mappers;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatuts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserRegistrationMapper {

    @Mapping(target = "id", ignore = true) // lo maneja la BD con autoincrement
    @Mapping(target = "passwordHash", source = "password") // password plano -> passwordHash
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "dateOfBirth", source = "birthDate")
    @Mapping(target = "role", expression = "java(UserRole.valueOf(dto.role().toUpperCase()))")
    @Mapping(target = "profilePictureUrl", ignore = true) // opcional, no viene del DTO
    @Mapping(target = "bio", ignore = true) // opcional
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "emailVerified", constant = "false") // al inicio no verificado
    @Mapping(target = "createdAt", ignore = true) // lo maneja @CreationTimestamp
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserRegistration dto);

    @Mapping(target = "password", ignore = true) // nunca devolvemos el password!
    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "dateOfBirth")
    UserRegistration toDto(User user);

}
