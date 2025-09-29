package com.labndbnb.landbnb.mappers.user;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {

    @Mapping(target = "dateBirth", source = "dateOfBirth")
    @Mapping(target = "userRole", source = "role")
    @Mapping(target = "userStatuts", source = "status")
    UserDto toDto(User user);

    @Mapping(target = "dateOfBirth", source = "dateBirth")
    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "status", source = "userStatuts")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserDto dto);
}
