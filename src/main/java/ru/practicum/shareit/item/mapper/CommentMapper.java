package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class CommentMapper {
    public Comment mapToComment(CommentDto commentDto, Item item, User author) {
        return new Comment(commentDto.getId(),
                commentDto.getText(),
                item,
                author,
                commentDto.getCreated());
    }

    public CommentDto mapToCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }
}