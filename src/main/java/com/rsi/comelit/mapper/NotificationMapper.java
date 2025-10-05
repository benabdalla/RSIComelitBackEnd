package com.rsi.comelit.mapper;

import com.rsi.comelit.dto.NotificationDto;
import com.rsi.comelit.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {UserSimpleMapper.class})
public interface NotificationMapper {
    NotificationDto toDto(Notification entity);

    Notification toEntity(NotificationDto dto);
}
