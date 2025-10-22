package poly.edu.repository;

import poly.edu.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    // Find by name
    Optional<Category> findByName(String name);
    
    // Find all categories ordered by name
    List<Category> findAllByOrderByNameAsc();
    
    // Find categories with products
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.quantity > 0")
    List<Category> findCategoriesWithAvailableProducts();
}



