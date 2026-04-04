package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String nickname,
            @PageableDefault(page = 1) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(nickname, pageable));
    }

    @GetMapping("/users/slice")
    public ResponseEntity<Slice<UserResponse>> getUsersWithSlice(
            @RequestParam String nickname,
            @PageableDefault(page = 1) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersWithSlice(nickname, pageable));
    }

    @GetMapping("/users/projection")
    public ResponseEntity<Page<UserResponse>> getUsersWithProjection(
            @RequestParam String nickname,
            @PageableDefault(page = 1) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersWithProjection(nickname, pageable));
    }
}
