package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.DataConflictException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> findCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return CategoryMapper.toDtos(categories);
    }

    @Override
    public CategoryDto findCategoryById(Long categoryId) {
        return CategoryMapper.toCategoryDto(getCategoryById(categoryId));
    }

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DataConflictException("Категория уже существует");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void removeCategory(Long categoryId) {
        getCategoryById(categoryId);
        if (eventRepository.existsByCategoryId((categoryId))) {
            throw new DataConflictException("В удаляемой категории есть события");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = getCategoryById(categoryId);
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }
}
