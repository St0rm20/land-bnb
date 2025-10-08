package com.labndbnb.landbnb.mappers.user;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {

    @Mapping(target = "userRole", source = "role")
    @Mapping(target = "userStatus", source = "status")
    UserDto toDto(User user);

    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "status", source = "userStatus")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserDto dto);
}
