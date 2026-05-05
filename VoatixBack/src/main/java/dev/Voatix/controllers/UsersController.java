package dev.Voatix.controllers;

import dev.Voatix.dto.user.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.user.UserCredentialsPasswordDTO;
import dev.Voatix.dto.user.ResponseUserProfileProjection;
import dev.Voatix.dto.user.UserSetAvatarDTO;
import dev.Voatix.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UsersController extends BaseController {

    final private UserService userService;

    @PostMapping("new")
    public void createUser(
            @RequestBody @Valid UserCredentialsPasswordDTO dto
    ) {
        userService.createUser(dto);
    }

    @PatchMapping("edit")
    public void updateUser(
            @RequestBody @Valid UpdateUserCredentialsPasswordDTO dto,
            Authentication auth
    ) {
        userService.editUser(dto, getUserId(auth));
    }

    @GetMapping("profile")
    public ResponseUserProfileProjection getMyProfile(Authentication auth) {
        return userService.getMyProfile(getUserId(auth));
    }

    @PatchMapping("avatar")
    public void updateAvatar(
            @Valid @RequestBody UserSetAvatarDTO dto,
            Authentication auth
            ) {
        userService.setAvatar(getUserId(auth), dto.getFileId());
    }
}
