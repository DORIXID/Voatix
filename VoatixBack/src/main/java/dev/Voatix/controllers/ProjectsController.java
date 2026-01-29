package dev.Voatix.controllers;


import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.repositories.ProjectRepository;
import dev.Voatix.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
