package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.model.Category;
import poly.edu.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    // Get categories with available products
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithAvailableProducts();
    }

    // Get category by ID
    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }

    // Get category by name
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Save category
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Delete category
    public void deleteCategory(Integer categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}



