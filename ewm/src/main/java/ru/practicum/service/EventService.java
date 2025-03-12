package ru.practicum.service;

import ru.practicum.dto.*;
import ru.practicum.model.Event;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    List<EventShortDto> findPublishedEvents(EventUserParam eventUserParam, HttpServletRequest request);

    EventFullDto findPublishedEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> findEventsOfUser(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto userUpdateEvent(Long userId, Long eventId, UpdateEventUserRequest eventUpdate);

    List<ParticipationRequestDto> findUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeEventRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventFullDto> findEventsByAdmin(EventAdminParam eventAdminParam);

    EventFullDto adminUpdateEvent(Long eventId, UpdateEventAdminRequest eventUpdate);

}
