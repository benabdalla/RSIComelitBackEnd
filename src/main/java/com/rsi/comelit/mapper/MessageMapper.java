package com.rsi.comelit.mapper;

import com.rsi.comelit.dto.MessageDto;
import com.rsi.comelit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", uses = {UserChatMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MessageMapper {
    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "recipient", target = "recipient")
    MessageDto toDto(Message entity);
}

