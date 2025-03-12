package ru.practicum.mapper;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EventMapper {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toNewEvent(NewEventDto newEventDto, User user, Category category) {
        Event event = Event.builder()
                .initiator(user)
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(category)
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER))
                .title(newEventDto.getTitle())
                .lon(newEventDto.getLocation().getLon())
                .lat(newEventDto.getLocation().getLat())
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .confirmedRequests(0)
                .build();

        if (newEventDto.getPaid() != null) {
            event.setIsPaid(newEventDto.getPaid());
        } else {
            event.setIsPaid(false);
        }
        if (newEventDto.getRequestModeration() != null) {
            event.setRequestModeration(newEventDto.getRequestModeration());
        } else {
            event.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        } else {
            event.setParticipantLimit(0);
        }

        return event;
    }

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto fullDto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(0)
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getIsPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .build();
        if (event.getPublishedOn() != null) {
            fullDto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }
        return fullDto;
    }

    public static EventFullDto toEventFullDtoWithViews(Event event, Map<Long, Long> eventViews) {
        EventFullDto fullDto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getIsPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(eventViews.get(event.getId()))
                .build();
        if (event.getPublishedOn() != null) {
            fullDto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }
        return fullDto;
    }

    public static List<EventFullDto> toFullDtos(Collection<Event> events, Map<Long, Long> eventViews) {
        return events.stream()
                .map(event -> toEventFullDtoWithViews(event, eventViews))
                .collect(Collectors.toList());
    }

    public static EventShortDto toEventShortDto(Event event, Map<Long, Long> eventViews) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate().format(FORMATTER))
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getIsPaid())
                .title(event.getTitle())
                .views(eventViews.get(event.getId()))
                .build();
    }

    public static List<EventShortDto> toShortDtos(List<Event> events, Map<Long, Long> eventViews) {
        return events.stream()
                .map(event -> toEventShortDto(event, eventViews))
                .collect(Collectors.toList());
    }
}
