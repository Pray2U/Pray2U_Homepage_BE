package com.pray2you.p2uhomepage.domain.reply.dto.response;

import com.pray2you.p2uhomepage.domain.post.entity.Post;
import com.pray2you.p2uhomepage.domain.reply.entity.Reply;
import com.pray2you.p2uhomepage.domain.user.dto.response.SimpleUserInfoResponseDTO;
import com.pray2you.p2uhomepage.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;

@Getter
public class DeleteReplyResponseDTO {

    private final long replyId;
    private final long postId;
    private final SimpleUserInfoResponseDTO user;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime deletedDate;

    @Builder
    private DeleteReplyResponseDTO(
            @NonNull Long replyId,
            @NonNull Long postId,
            @NonNull SimpleUserInfoResponseDTO user,
            @NonNull String content,
            @NonNull LocalDateTime createdDate,
            @NonNull LocalDateTime deletedDate) {
        this.replyId = replyId;
        this.postId = postId;
        this.user = user;
        this.content = content;
        this.createdDate = createdDate;
        this.deletedDate = deletedDate;
    }

    public static DeleteReplyResponseDTO toDTO(Reply reply) {
        return DeleteReplyResponseDTO.builder()
                .replyId(reply.getId())
                .postId(reply.getPost().getId())
                .content(reply.getContent())
                .user(SimpleUserInfoResponseDTO.toDTO(reply.getUser()))
                .createdDate(reply.getCreatedDate())
                .deletedDate(reply.getModifiedDate())
                .build();
    }
}