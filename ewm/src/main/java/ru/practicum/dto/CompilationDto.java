package ru.practicum.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
