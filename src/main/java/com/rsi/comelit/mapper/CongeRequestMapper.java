package com.rsi.comelit.mapper;

import com.rsi.comelit.dto.CongeRequestDTO;
import com.rsi.comelit.entity.CongeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserSimpleMapper.class, CongeRequestCommentMapper.class})
public interface CongeRequestMapper {
    CongeRequestDTO toDto(CongeRequest entity);

    CongeRequest toEntity(CongeRequestDTO dto);
}
