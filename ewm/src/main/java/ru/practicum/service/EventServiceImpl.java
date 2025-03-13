package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.dto.*;
import ru.practicum.model.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventStatService eventStatService;
    private final RequestRepository requestRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventShortDto> findPublishedEvents(EventUserParam eventUserParam, HttpServletRequest request) {
        Sort sort;
        List<Event> events;
        Map<Long, Long> views;
        sort = getEventSort(eventUserParam.getSort());
        Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(),
                eventUserParam.getSize(), sort);
        LocalDateTime checkedRangeStart = validateRangeTime(eventUserParam.getRangeStart(), eventUserParam.getRangeEnd());
        Specification<Event> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));
            if (eventUserParam.getText() != null) {
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                                "%" + eventUserParam.getText().toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + eventUserParam.getText().toLowerCase() + "%")));
            }
            if (eventUserParam.getCategories() != null) {
                CriteriaBuilder.In<Long> categoriesClause = criteriaBuilder.in(root.get("category").get("id"));
                for (Long category : eventUserParam.getCategories()) {
                    categoriesClause.value(category);
                }
                predicates.add(categoriesClause);
            }
            if (eventUserParam.getPaid() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPaid"), eventUserParam.getPaid()));
            }
            predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), checkedRangeStart));
            if (eventUserParam.getRangeEnd() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventUserParam.getRangeEnd()));
            }
            if (eventUserParam.getOnlyAvailable() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("confirmedRequests"), root.get("participantLimit")));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }
        );
        events = eventRepository.findAll(specification, pageable).getContent();
        views = eventStatService.getEventsViews(events.stream().map(Event::getId).toList());
        return EventMapper.toShortDtos(events, views);
    }

    @Override
    public EventFullDto findPublishedEventById(Long eventId, HttpServletRequest request) {
        Map<Long, Long> views = eventStatService.getEventsViews(List.of(eventId));
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        return EventMapper.toEventFullDtoWithViews(event, views);
    }

    @Override
    public List<EventShortDto> findEventsOfUser(Long userId, Integer from, Integer size) {
        Map<Long, Long> views;
        List<EventShortDto> userEvents;
        getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        views = eventStatService.getEventsViews(events.stream().map(Event::getId).toList());
        userEvents = EventMapper.toShortDtos(events, views);
        return userEvents;
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = getUserById(userId);
        Category category = getCategoryById(newEventDto.getCategory());
        Event event = EventMapper.toNewEvent(newEventDto, user, category);
        validateEventTimeByUser(event.getEventDate());
        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(event));
        eventFullDto.setViews(0L);
        return eventFullDto;
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Map<Long, Long> views = eventStatService.getEventsViews(List.of(eventId));
        return EventMapper.toEventFullDtoWithViews(event, views);
    }

    @Transactional
    @Override
    public EventFullDto userUpdateEvent(Long userId, Long eventId, UpdateEventUserRequest eventUpdate) {
        Event updated;
        Map<Long, Long> views;
        Category category;
        getUserById(userId);
        Event oldEvent = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (oldEvent.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("нельзя изменить опубликованное событие");
        }
        if (eventUpdate.getEventDate() != null) {
            LocalDateTime updateEventTime = LocalDateTime.parse(eventUpdate.getEventDate(), FORMATTER);
            validateEventTimeByUser(updateEventTime);
        }
        if (eventUpdate.getStateAction() != null) {
            updateEventByUserStateAction(oldEvent, eventUpdate);
        }
        if (eventUpdate.getAnnotation() != null) {
            oldEvent.setAnnotation(eventUpdate.getAnnotation());
        }
        if (eventUpdate.getCategory() != null) {
            category = getCategoryById(eventUpdate.getCategory());
            oldEvent.setCategory(category);
        }
        if (eventUpdate.getDescription() != null) {
            oldEvent.setDescription(eventUpdate.getDescription());
        }
        if (eventUpdate.getLocation() != null) {
            oldEvent.setLat(eventUpdate.getLocation().getLat());
            oldEvent.setLon(eventUpdate.getLocation().getLon());
        }
        if (eventUpdate.getPaid() != null) {
            oldEvent.setIsPaid(eventUpdate.getPaid());
        }
        if (eventUpdate.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(eventUpdate.getParticipantLimit());
        }
        if (eventUpdate.getRequestModeration() != null) {
            oldEvent.setRequestModeration(eventUpdate.getRequestModeration());
        }
        if (eventUpdate.getTitle() != null) {
            oldEvent.setTitle(eventUpdate.getTitle());
        }
        updated = eventRepository.save(oldEvent);
        views = eventStatService.getEventsViews(List.of(eventId));
        return EventMapper.toEventFullDtoWithViews(updated, views);
    }

    @Override
    public List<ParticipationRequestDto> findUserEventRequests(Long userId, Long eventId) {
        getUserById(userId);
        getEventById(eventId);
        List<Request> eventRequests = requestRepository.findAllByEventId(eventId);
        return RequestMapper.toDtos(eventRequests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeEventRequestsStatus(Long userId, Long eventId,
                                                                    EventRequestStatusUpdateRequest statusUpdate) {
        int requestsCount = statusUpdate.getRequestIds().size();
        getUserById(userId);
        Event event = getEventById(eventId);
        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();
        RequestStatus status = RequestStatus.valueOf(statusUpdate.getStatus());
        List<Request> requests = requestRepository.findByIdIn(statusUpdate.getRequestIds());

        if (!Objects.equals(userId, event.getInitiator().getId())) {
            throw new NotFoundException("У пользователя нет указанного события");
        }
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataConflictException("Изменить статус можно только у заявки, ожидающей подтверждения");
            }
        }
        switch (status) {
            case CONFIRMED:
                if (event.getParticipantLimit() == 0 || !event.getRequestModeration()
                        || event.getParticipantLimit() > event.getConfirmedRequests() + requestsCount) {
                    requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
                    event.setConfirmedRequests(event.getConfirmedRequests() + requestsCount);
                    confirmed.addAll(requests);
                } else if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                    throw new DataConflictException("Достигнут лимит заявок на участие в событии");
                } else {
                    for (Request request : requests) {
                        if (event.getParticipantLimit() > event.getConfirmedRequests()) {
                            request.setStatus(RequestStatus.CONFIRMED);
                            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                            confirmed.add(request);
                        } else {
                            request.setStatus(RequestStatus.REJECTED);
                            rejected.add(request);
                        }
                    }
                }
                break;
            case REJECTED:
                requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
                rejected.addAll(requests);
        }
        eventRepository.save(event);
        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(RequestMapper.toDtos(confirmed),
                RequestMapper.toDtos(rejected));
    }

    @Override
    public List<EventFullDto> findEventsByAdmin(EventAdminParam eventAdminParam) {
        List<Event> events;
        Map<Long, Long> views;
        Pageable pageable = PageRequest.of(eventAdminParam.getFrom() / eventAdminParam.getSize(), eventAdminParam.getSize());
        Specification<Event> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventAdminParam.getUsers() != null) {
                CriteriaBuilder.In<Long> usersClause = criteriaBuilder.in(root.get("initiator").get("id"));
                for (Long user : eventAdminParam.getUsers()) {
                    usersClause.value(user);
                }
                predicates.add(usersClause);
            }
            if (eventAdminParam.getStates() != null) {
                List<EventState> states = getEventStates(eventAdminParam.getStates());
                CriteriaBuilder.In<EventState> statesClause = criteriaBuilder.in(root.get("state"));
                for (EventState state : states) {
                    statesClause.value(state);
                }
                predicates.add(statesClause);
            }
            if (eventAdminParam.getCategories() != null) {
                CriteriaBuilder.In<Long> categoriesClause = criteriaBuilder.in(root.get("category").get("id"));
                for (Long category : eventAdminParam.getCategories()) {
                    categoriesClause.value(category);
                }
                predicates.add(categoriesClause);
            }
            if (eventAdminParam.getRangeStart() != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), eventAdminParam.getRangeStart()));
            }
            if (eventAdminParam.getRangeEnd() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventAdminParam.getRangeEnd()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }
        );
        events = eventRepository.findAll(specification, pageable).getContent();
        views = eventStatService.getEventsViews(events.stream().map(Event::getId).collect(Collectors.toList()));
        return EventMapper.toFullDtos(events, views);
    }

    @Override
    @Transactional
    public EventFullDto adminUpdateEvent(Long eventId, UpdateEventAdminRequest eventUpdate) {
        Event updated;
        Map<Long, Long> views;
        Category category;
        Event oldEvent = getEventById(eventId);

        if (eventUpdate.getEventDate() != null) {
            LocalDateTime updateTime = LocalDateTime.parse(eventUpdate.getEventDate(), FORMATTER);
            validateEventTimeByAdmin(updateTime);
        }
        if (eventUpdate.getStateAction() != null) {
            validateEventState(oldEvent.getState());
            updateEventByAdminStateAction(oldEvent, eventUpdate);
        }
        if (eventUpdate.getAnnotation() != null) {
            oldEvent.setAnnotation(eventUpdate.getAnnotation());
        }
        if (eventUpdate.getCategory() != null) {
            category = getCategoryById(eventUpdate.getCategory());
            oldEvent.setCategory(category);
        }
        if (eventUpdate.getDescription() != null) {
            oldEvent.setDescription(eventUpdate.getDescription());
        }
        if (eventUpdate.getLocation() != null) {
            oldEvent.setLat(eventUpdate.getLocation().getLat());
            oldEvent.setLon(eventUpdate.getLocation().getLon());
        }
        if (eventUpdate.getPaid() != null) {
            oldEvent.setIsPaid(eventUpdate.getPaid());
        }
        if (eventUpdate.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(eventUpdate.getParticipantLimit());
        }
        if (eventUpdate.getRequestModeration() != null) {
            oldEvent.setRequestModeration(eventUpdate.getRequestModeration());
        }
        if (eventUpdate.getTitle() != null) {
            oldEvent.setTitle(eventUpdate.getTitle());
        }
        updated = eventRepository.save(oldEvent);
        views = eventStatService.getEventsViews(List.of(eventId));
        return EventMapper.toEventFullDtoWithViews(updated, views);
    }

    private Sort getEventSort(String eventSort) {
        EventSort sort;
        if (eventSort == null) {
            return Sort.by("id");
        }
        try {
            sort = EventSort.valueOf(eventSort);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный тип сортировки событий");
        }
        return switch (sort) {
            case EVENT_DATE -> Sort.by("eventDate");
            case VIEWS -> Sort.by("views");
        };
    }

    private LocalDateTime validateRangeTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Начало временного промежутка должно быть до его конца");
        } else return Objects.requireNonNullElseGet(rangeStart, LocalDateTime::now);
    }

    private void validateEventTimeByAdmin(LocalDateTime eventTime) {
        if(eventTime.isBefore((LocalDateTime.now()))) {
            throw new BadRequestException("Нельзя изменить дату события на уже наступившую");
        }
        if (eventTime.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DataConflictException("Дата начала изменяемого события должна быть не ранее чем за час");
        }
    }

    private void validateEventTimeByUser(LocalDateTime eventTime) {
        if(eventTime.isBefore((LocalDateTime.now()))) {
            throw new BadRequestException("Нельзя изменить дату события на уже наступившую");
        }
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataConflictException("Дата и время на которые намечено событие не может быть " +
                    "раньше, чем через два часа");
        }
    }

    private void validateEventState(EventState state) {
        if (!state.equals(EventState.PENDING)) {
            throw new DataConflictException("Событие находится не в состоянии ожидания публикации");
        }
    }

    private void updateEventByAdminStateAction(Event oldEvent, UpdateEventAdminRequest eventUpdate) {
        AdminEventStateAction stateAction;
        try {
            stateAction = AdminEventStateAction.valueOf(eventUpdate.getStateAction());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Неизвестный параметр " + eventUpdate.getStateAction());
        }
        switch (stateAction) {
            case REJECT_EVENT:
                if (oldEvent.getState().equals(EventState.PUBLISHED)) {
                    throw new DataConflictException("Невозможно отклонить уже опубликованные события");
                }
                oldEvent.setState(EventState.CANCELED);
                break;
            case PUBLISH_EVENT:
                if (!oldEvent.getState().equals(EventState.PENDING)) {
                    throw new DataConflictException("Опубликовать можно только события в состоянии ожидания публикации");
                }
                oldEvent.setState(EventState.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
                break;
            default:
                throw new BadRequestException("Неизвестный параметр состояния события");
        }
    }

    private void updateEventByUserStateAction(Event oldEvent, UpdateEventUserRequest eventUpdate) {
        UserEventStateAction stateAction;
        try {
            stateAction = UserEventStateAction.valueOf(eventUpdate.getStateAction());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Неизвестный параметр " + eventUpdate.getStateAction());
        }
        switch (stateAction) {
            case SEND_TO_REVIEW:
                oldEvent.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                oldEvent.setState(EventState.CANCELED);
                break;
            default:
                throw new BadRequestException("Неизвестный параметр состояния события");
        }
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private List<EventState> getEventStates(List<String> states) {
        List<EventState> eventStates = new ArrayList<>();
        try {
            for (String state : states) {
                eventStates.add(EventState.valueOf(state));
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный параметр состояния события");
        }
        return eventStates;
    }
}
