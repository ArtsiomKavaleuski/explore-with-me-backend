package ru.practicum.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class UserWithFollowersDto {
    private Long id;
    private String name;
    private String email;
    private List<UserDto> followers;
}
