package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    @Email
    @NotBlank
    @Size(min = 6, max = 254, message = "Длина эл. почты должна быть от 6 до 254")
    private String email;
    @NotBlank
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250")
    private String name;
}
