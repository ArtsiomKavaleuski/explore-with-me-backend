package ru.practicum.service;

import ru.practicum.dto.UserDto;
import ru.practicum.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> findUsers(List<Long> ids, Integer from, Integer size);

    UserDto addUser(UserDto inDto);

    void deleteUser(Long userId);

}
