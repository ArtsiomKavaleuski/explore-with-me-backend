package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserWithFollowersDto;
import ru.practicum.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public List<UserDto> findUsers(@RequestParam(required = false) List<Long> ids,
                                   @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
                                   @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {
        return userService.findUsers(ids, from, size);
    }

    @PostMapping(value = "/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(userDto);
    }

    @DeleteMapping(value = "/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@NotNull @PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PostMapping(value = "/users/{userId}/followers/{followerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserWithFollowersDto addFollower(@PathVariable Long userId, @PathVariable Long followerId) {
        return userService.addFollower(userId, followerId);
    }

    @DeleteMapping(value = "/users/{userId}/followers/{followerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFollower(@PathVariable Long userId, @PathVariable Long followerId) {
        userService.deleteFollower(userId, followerId);
    }
}
