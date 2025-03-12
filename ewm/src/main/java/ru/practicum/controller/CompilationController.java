package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compService;

    @GetMapping("/compilations")
    public List<CompilationDto> findCompilations(@RequestParam(required = false, defaultValue = "false") String pinned,
                                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return compService.findCompilations(Boolean.valueOf(pinned), from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        return compService.findCompilationById(compId);
    }

    @PostMapping(value = "/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto compilationDto) {
        return compService.addCompilation(compilationDto);
    }

    @DeleteMapping(value = "/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compService.deleteCompilation(compId);
    }

    @PatchMapping(value = "/admin/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        return compService.updateCompilation(compId, updateRequest);
    }
}
