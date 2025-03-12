package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.model.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.EventRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventStatService statService;

    @Override
    public List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size) {
        Set<Event> eventSet = new HashSet<>();
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.findAllByIsPinned(pinned, pageable);
        for (Compilation compilation : compilations) {
            eventSet.addAll(compilation.getEvents());
        }
        List<Long> eventIds = eventSet.stream().map(Event::getId).toList();
        Map<Long, Long> views = statService.getEventsViews(eventIds);
        return CompilationMapper.toDtos(compilations, views);
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        Compilation compilation = getCompilationById(compId);
        List<Long> events = compilation.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> views = statService.getEventsViews(events);
        return CompilationMapper.toCompilationDto(compilation, views);
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        List<Event> events;
        Map<Long, Long> views = new HashMap<>();
        if (compilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilationDto.getEvents());
            views = statService.getEventsViews(events.stream().map(Event::getId).toList());
        } else {
            events = new ArrayList<>();
        }
        Compilation compilation = CompilationMapper.toNewCompilation(compilationDto, events);
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation, views);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        getCompilationById(compId);
        compilationRepository.deleteById(compId.intValue());

    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest update) {
        Compilation oldCompilation = getCompilationById(compId);
        Map<Long, Long> views = new HashMap<>();
        if (update.getEvents() != null && !update.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(update.getEvents());
            oldCompilation.setEvents(events);
            views = statService.getEventsViews(update.getEvents());
        }
        if (update.getPinned() != null) {
            oldCompilation.setIsPinned(update.getPinned());
        }
        if (update.getTitle() != null) {
            oldCompilation.setTitle(update.getTitle());
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(oldCompilation), views);
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId.intValue())
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
    }
}
