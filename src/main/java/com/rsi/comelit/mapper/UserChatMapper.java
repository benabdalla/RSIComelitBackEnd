package com.rsi.comelit.mapper;

import com.rsi.comelit.dto.UserChatDto;
import com.rsi.comelit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserChatMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(target = "name", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(source = "online", target = "online")
    UserChatDto toDto(User entity);
}
