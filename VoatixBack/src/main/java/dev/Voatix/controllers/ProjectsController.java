package dev.Voatix.controllers;


import dev.Voatix.dto.project.*;
import dev.Voatix.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/projects")
public class ProjectsController extends BaseController {

    final private ProjectService projectService;

    @GetMapping("")
    public List<ProjectOfUserDTO> getProjectsOfUser(Authentication auth) {
        return projectService.getProjectsOfUser(getUserId(auth));
    }

    @GetMapping("{projectId}")
    public ProjectOfUserDTO getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @GetMapping("{id}/profile")
    public ProjectProfileDTO getProfileOfProject(@PathVariable Long id, Authentication auth) {
        return projectService.getProjectProfile(id, getUserId(auth));
    }

    @PatchMapping("avatar")
    public void updateAvatar(
            Authentication auth,
            @Valid @RequestBody ProjectAvatarUpdateDTO dto) {
        projectService.setAvatar(getUserId(auth), dto);
    }

    @DeleteMapping("moderators")
    public void deleteModerator(
            @Valid @RequestBody ProjectModeratorDTO dto,
            Authentication auth) {
        projectService.deleteModerator(getUserId(auth), dto);
    }

    @PostMapping("moderators")
    public void addModerator(
            @Valid @RequestBody ProjectModeratorDTO dto,
            Authentication auth) {
        projectService.addModerator(getUserId(auth), dto);
    }

    @PostMapping("")
    public void createProject(
            @Valid @RequestBody ProjectCreateDTO dto,
            Authentication auth) {
        projectService.createProject(getUserId(auth), dto);
    }

    @DeleteMapping("")
    public void deleteProject(
            @Valid @RequestBody ProjectDeleteDTO dto,
            Authentication auth
    ){
        projectService.deleteProject(getUserId(auth), dto.getId());
    }
}
