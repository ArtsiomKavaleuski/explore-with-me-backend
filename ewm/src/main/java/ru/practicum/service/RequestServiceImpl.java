package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.*;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.mapper.RequestMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> findUserRequests(Long userId) {
        findUserById(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return RequestMapper.toDtos(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Request request;
        User requester = findUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        List<Request> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        if (!requests.isEmpty()) {
            throw new DataConflictException("Запрос уже существует");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new DataConflictException("Создатель события не может быть его участником");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new DataConflictException("Достигнут лимит запросов на участие");
        }
        request = RequestMapper.toRequest(requester, event);
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        request = requestRepository.save(request);
        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto rejectRequest(Long userId, Long requestId) {
        findUserById(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return RequestMapper.toRequestDto(request);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
