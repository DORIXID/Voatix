package dev.Voatix.controllers.https;

import dev.Voatix.dto.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.dto.projection.ResponseUserProjection;
import dev.Voatix.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("api/users")
public class UsersController {
    @Autowired
    private UserService userService;

    @PostMapping("new")
    public void createUser(
            @RequestBody @Valid UserCredentialsPasswordDTO dto
    ) {
        userService.createUser(dto);
    }

    @PatchMapping("edit")
    public void updateUser(
            @RequestBody @Valid UpdateUserCredentialsPasswordDTO dto,
            Principal principal
    ) {
        userService.editUser(dto, principal);
    }

    @GetMapping("profile")
    public ResponseUserProjection getMyProfile(Principal principal) {
        return userService.getMyProfile(principal);
    }

    @PatchMapping("avatar")
    public void updateAvatar(Principal principal, @RequestParam("key") String key) {
        userService.setAvatar(principal, key);
    }

}
