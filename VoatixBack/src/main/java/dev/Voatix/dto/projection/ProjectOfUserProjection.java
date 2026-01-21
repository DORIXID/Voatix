package dev.Voatix.dto.projection;

public interface ProjectOfUserProjection {
    Long getProjectId();
    String getTitle();
    Long getAvatarId();
    Boolean getActive();
    String getRoleOfUser();
}