package poly.edu.repository;

import poly.edu.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Search by name
    List<Product> findByNameContainingIgnoreCase(String keyword);
    
    // Find by category with pagination
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    
    // Find by category
    List<Product> findByCategoryId(Integer categoryId);
    
    // Find available products (quantity > 0)
    Page<Product> findByQuantityGreaterThan(Integer quantity, Pageable pageable);
    
    // Find out of stock products
    List<Product> findByQuantityLessThanEqual(Integer quantity);
    
    // Find by price range
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    // Find by rating
    Page<Product> findByRatingGreaterThanEqual(Double rating, Pageable pageable);
    
    // Search products with pagination
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Find products by category and availability
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.quantity > 0")
    Page<Product> findAvailableProductsByCategory(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    // Find featured products (high rating and available)
    @Query("SELECT p FROM Product p WHERE p.rating >= 4.0 AND p.quantity > 0 ORDER BY p.rating DESC")
    Page<Product> findFeaturedProducts(Pageable pageable);
}
