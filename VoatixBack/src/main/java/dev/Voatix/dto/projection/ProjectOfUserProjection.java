package dev.Voatix.dto.projection;

public interface ProjectOfUserProjection {
    Long getProjectId();
    String getTitle();
    String getKey();
    Boolean getActive();
    String getRoleOfUser();
}