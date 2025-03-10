package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;

public interface EwmStatSrvService {
    EndpointHitDto saveStat(EndpointHitDto endpointHitDto);

    Collection<ViewStatsDto> getStats(LocalDateTime start,
                                      LocalDateTime end,
                                      Collection<String> uris,
                                      Boolean unique);
}