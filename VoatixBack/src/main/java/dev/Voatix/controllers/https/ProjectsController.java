package dev.Voatix.controllers.https;


import dev.Voatix.dto.ProjectCreateDTO;
import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.dto.ProjectProfileDTO;
import dev.Voatix.service.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/projects")
public class ProjectsController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("")
    public List<ProjectOfUserDTO> getProjectsOfUser(Principal principal) {
        return projectService.getProjectsOfUser(principal);
    }

    @GetMapping("{title}")
    public ProjectOfUserDTO getProject(@PathVariable String title) {
        return projectService.getProject(title);
    }

    @GetMapping("{title}/profile")
    public ProjectProfileDTO getModeratorsOfProject(@PathVariable String title, Principal principal) {
        return projectService.getProjectProfile(title, principal);
    }

    @PatchMapping("avatar")
    public void updateAvatar(Principal principal,
                             @RequestParam("key") String key,
                             @RequestParam("title") String title) {
        projectService.setAvatar(principal, title, key);
    }

    @DeleteMapping("{title}/moderators/{nickname}")
    public void deleteModerator(
                                @PathVariable String title,
                                @PathVariable String nickname,
                                Principal principal) {
        projectService.deleteModerator(title, nickname, principal);
    }

    @PostMapping("{title}/moderators/{nickname}")
    public void addModerator(
            @PathVariable String title,
            @PathVariable String nickname,
            Principal principal) {
        projectService.addModerator(title, nickname, principal);
    }

    @PostMapping("")
    public void createProject(
            @Valid @RequestBody ProjectCreateDTO dto,
            Principal principal) {
        projectService.createProject(dto, principal);
    }

    @DeleteMapping("{title}")
    public void deleteProject(
            @PathVariable String title,
            Principal principal
    ){
        projectService.deleteProject(title, principal);
    }
}
