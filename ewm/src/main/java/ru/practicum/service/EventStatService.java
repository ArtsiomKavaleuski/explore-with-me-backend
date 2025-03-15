package ru.practicum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.EwmStatClient;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.mapper.EventMapper.FORMATTER;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatService {

    private final EwmStatClient statisticClient;
    private final ObjectMapper objectMapper;
    private final Gson gson;


    public static final String EVENT_URI = "/events/";

    public Map<Long, Long> getEventsViews(List<Long> events) {
        List<ViewStatsDto> stats;
        Map<Long, Long> eventsViews = new HashMap<>();
        List<String> uris = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return eventsViews;
        }
        for (Long id : events) {
            uris.add(EVENT_URI + id);
        }
        ResponseEntity<Object> response = statisticClient.getStat(LocalDateTime.now().minusDays(100).format(FORMATTER),
                LocalDateTime.now().format(FORMATTER), uris, true);
        Object body = response.getBody();
        if (body != null) {
            String json = gson.toJson(body);
            TypeReference<List<ViewStatsDto>> typeRef = new TypeReference<>() {
            };
            try {
                stats = objectMapper.readValue(json, typeRef);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Ошибка при загрузке данных из сервиса статистики");
            }
            for (Long event : events) {
                eventsViews.put(event, 0L);
            }
            if (!stats.isEmpty()) {
                for (ViewStatsDto stat : stats) {
                    eventsViews.put(Long.parseLong(stat.getUri().split("/", 0)[2]),
                            stat.getHits());
                }
            }
        }
        return eventsViews;
    }
}
