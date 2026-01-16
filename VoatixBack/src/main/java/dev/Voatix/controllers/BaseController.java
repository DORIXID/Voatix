package dev.Voatix.controllers;

import dev.Voatix.dto.IdeaDTO;
import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.service.IdeaService;
import dev.Voatix.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/base")
public class BaseController {

    @Autowired
    private IdeaService ideaService;
    @Autowired
    private ProjectService projectService;



    //http://localhost:8080/api/base/ideas?project=CoffeeWay&page=0&limit=10&filterBy=ALL&searchedValue=
    @GetMapping("ideas")
    public Page<IdeaWithStatsDTO> getIdeas(
            @RequestParam(required = true) String project,
            @RequestParam() Integer page,
            @RequestParam(defaultValue = "12") Integer limit,
            @RequestParam(defaultValue = "", required = false) String filterBy,
            @RequestParam(required = false) String searchedValue,
            Principal principal) {
        return  ideaService.getIdeas(
                project,
                page,
                limit,
                filterBy,
                searchedValue,
                principal);
    }

    @GetMapping("projects")
    public List<ProjectOfUserDTO> getProjectsOfUser(Principal principal) {
        return projectService.getProjectsOfUser(principal);
    }

    //http://localhost:8080/api/base/ideas/1/likes?isLike=true
    @PutMapping("ideas/{ideaId}/likes")
    public ResponseEntity<Void> upsertIdeaLike(
        @PathVariable() Long ideaId,
        @RequestParam(required = false) Boolean isLike,
        Principal principal){
        ideaService.upsertLike(ideaId, isLike, principal);
        return ResponseEntity.ok().build();
    }
}
