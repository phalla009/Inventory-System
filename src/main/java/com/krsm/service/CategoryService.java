package com.krsm.service;

import com.krsm.entity.Category;
import com.krsm.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAllCategories() {
        return repository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return repository.findById(id);
    }

    public void saveCategory(Category category) {
        repository.save(category);
    }

    public void deleteCategory(Long id) {
        repository.deleteById(id);
    }

	public boolean hasProducts(Long id) {
		// TODO Auto-generated method stub
		return false;
	}
}
