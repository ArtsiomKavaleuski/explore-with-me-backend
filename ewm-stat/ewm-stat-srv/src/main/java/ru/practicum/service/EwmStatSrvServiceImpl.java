package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.mapper.ViewStatsMapper;
import ru.practicum.repository.EwmStatSrvRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EwmStatSrvServiceImpl implements EwmStatSrvService {

    private final EwmStatSrvRepository repository;

    @Override
    @Transactional
    public EndpointHitDto saveStat(EndpointHitDto endpointHitDto) {
        return EndpointHitMapper
                .toEndpointHitDto(repository.save(EndpointHitMapper.toEndpointHit(endpointHitDto))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ViewStatsDto> getStats(LocalDateTime start,
                                             LocalDateTime end,
                                             Collection<String> uris,
                                             Boolean unique) {
        checkDates(start, end);
        if ((uris == null || uris.isEmpty()) && !unique) {
            return ViewStatsMapper.toViewStatsDto(repository.findStatsByTime(start, end));
        } else if (unique && (uris == null || uris.isEmpty())) {
            return ViewStatsMapper.toViewStatsDto(repository.findStatsByTimeAndUniqIp(start, end));
        } else if (unique) {
            return ViewStatsMapper.toViewStatsDto(repository.findStatsByTimeAndUrisAndUniqIp(start, end, uris));
        } else {
            return ViewStatsMapper.toViewStatsDto(repository.findStatsByTimeAndUris(start, end, uris));
        }
    }

    private void checkDates(LocalDateTime start, LocalDateTime end) {
        if (start.isBefore(end)) {
            throw new BadRequestException("Дата начала не может быть после даты конца");
        }
    }
}