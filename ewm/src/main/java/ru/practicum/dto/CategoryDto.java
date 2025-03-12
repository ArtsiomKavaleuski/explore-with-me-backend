package ru.practicum.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Long id;
    @NotBlank
    @Size(min = 1, max = 50, message = "Длина названия категории должна быть от 1 до 50")
    private String name;
}
