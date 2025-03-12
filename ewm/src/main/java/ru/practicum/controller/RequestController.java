package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> findUserRequests(@PathVariable Long userId) {
        return requestService.findUserRequests(userId);
    }

    @PostMapping(value = "/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @NotNull @RequestParam Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping(value = "/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto rejectRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.rejectRequest(userId, requestId);
    }
}
