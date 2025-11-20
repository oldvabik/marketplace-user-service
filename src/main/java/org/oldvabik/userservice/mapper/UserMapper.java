package org.oldvabik.userservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.oldvabik.userservice.dto.UserCreateDto;
import org.oldvabik.userservice.dto.UserDto;
import org.oldvabik.userservice.dto.UserUpdateDto;
import org.oldvabik.userservice.entity.User;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface UserMapper {
    UserDto toDto(User entity);
    User toEntity(UserCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User entity);
}
