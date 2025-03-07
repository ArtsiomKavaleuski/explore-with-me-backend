package ru.practicum.mapper;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.ViewStats;

import java.util.Collection;
import java.util.stream.Collectors;

public class ViewStatsMapper {
    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }

    public static Collection<ViewStatsDto> toViewStatsDto(Collection<ViewStats> viewStat) {
        return viewStat.stream().map(ViewStatsMapper::toViewStatsDto).collect(Collectors.toList());
    }
}
