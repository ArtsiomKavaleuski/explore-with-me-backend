package ru.practicum.mapper;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static Compilation toNewCompilation(NewCompilationDto dto, List<Event> events) {
        Compilation compilation = Compilation.builder()
                .events(events)
                .title(dto.getTitle())
                .build();
        if (dto.getPinned() != null) {
            compilation.setIsPinned(dto.getPinned());
        } else {
            compilation.setIsPinned(false);
        }
        return compilation;
    }

    public static CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(EventMapper.toShortDtos(compilation.getEvents(), views))
                .pinned(compilation.getIsPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static List<CompilationDto> toDtos(List<Compilation> compilations, Map<Long, Long> views) {
        return compilations.stream()
                .map(compilation -> toCompilationDto(compilation, views)).collect(Collectors.toList());
    }
}
