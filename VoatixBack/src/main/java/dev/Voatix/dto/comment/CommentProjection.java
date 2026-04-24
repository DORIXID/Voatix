package dev.Voatix.dto.comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentProjection {
    Long getId();
    String getText();
    LocalDateTime getDateTime();
    Long getUser();
    Long getIdea();
    List<Long> getFileIds();
}