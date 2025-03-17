package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserWithFollowersDto;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> findUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        Pageable pageRequest = PageRequest.of(from / size, size);
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageRequest).getContent();
        } else {
            users = userRepository.findByIdIn(ids, pageRequest);
        }
        return UserMapper.toUserDtos(users);
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        findById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public UserWithFollowersDto addSubscription(Long userId, Long followeeId) {
        if (userId.equals(followeeId)) {
            throw new DataConflictException("Пользователь не может подписаться на самого себя");
        }
        User user = findById(userId);
        User followee = findById(followeeId);

        if (user.getFollowees().contains(followee)) {
            throw new DataConflictException("Подписка уже существует");
        }
        user.getFollowees().add(followee);
        user = userRepository.save(user);
        return UserMapper.toDtoWithFollowees(user);
    }

    @Transactional
    @Override
    public void deleteSubscription(Long userId, Long followeeId) {
        if (userId.equals(followeeId)) {
            throw new DataConflictException("Попытка удалить подписку на самого себя");
        }
        User user = findById(userId);
        User followee = findById(followeeId);

        if (!user.getFollowees().contains(followee)) {
            throw new DataConflictException("Подписки не существует");
        }
        user.getFollowees().remove(followee);
        userRepository.save(user);
    }
}