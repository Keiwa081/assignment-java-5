package poly.edu.repository;

import poly.edu.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Lấy tất cả tên categories
    @Query("SELECT c.name FROM Category c")
    List<String> findAllCategoryNames();

    // Tìm category theo tên
    Category findByName(String name);
}