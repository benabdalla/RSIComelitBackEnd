package com.rsi.comelit.mapper;

import com.rsi.comelit.dto.CongeRequestCommentDTO;
import com.rsi.comelit.entity.CongeRequestComment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CongeRequestCommentMapper {
    CongeRequestCommentDTO toDto(CongeRequestComment entity);

    CongeRequestComment toEntity(CongeRequestCommentDTO dto);
}
