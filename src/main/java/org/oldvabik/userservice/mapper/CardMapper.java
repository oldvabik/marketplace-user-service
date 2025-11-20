package org.oldvabik.userservice.mapper;

import org.mapstruct.*;
import org.oldvabik.userservice.dto.CardCreateDto;
import org.oldvabik.userservice.dto.CardDto;
import org.oldvabik.userservice.dto.CardUpdateDto;
import org.oldvabik.userservice.entity.Card;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card entity);

    @Mapping(target = "user.id", source = "userId")
    Card toEntity(CardCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CardUpdateDto dto, @MappingTarget Card entity);
}
