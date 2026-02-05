package dev.Voatix.controllers;

import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/users")
public class UsersController {
    @Autowired
    private UserService userService;

    @PostMapping("new")
    public ResponseEntity<Void> createUser(
            @RequestBody @Valid UserCredentialsPasswordDTO dto
    ) {
        userService.createUser(dto);
        return ResponseEntity.ok().build();
    }
}
