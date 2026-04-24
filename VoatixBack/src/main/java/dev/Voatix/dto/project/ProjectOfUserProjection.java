package dev.Voatix.dto.project;

public interface ProjectOfUserProjection {
    Long getProjectId();
    String getTitle();
    Long getFileId();
    Boolean getActive();
    String getRoleOfUser();
}