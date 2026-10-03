package com.edumerge.campuscare.dto;

import com.edumerge.campuscare.entity.CommentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentCreateDto {

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    @NotNull(message = "Comment visibility must be specified")
    private CommentVisibility visibility = CommentVisibility.PUBLIC;

    public CommentCreateDto() {
    }

    public CommentCreateDto(String content, CommentVisibility visibility) {
        this.content = content;
        this.visibility = visibility;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CommentVisibility visibility) {
        this.visibility = visibility;
    }
}
