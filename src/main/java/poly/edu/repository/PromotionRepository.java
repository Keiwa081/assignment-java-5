package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.model.Promotion;

import java.util.Date;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
    // Tìm promotion đang active
    List<Promotion> findByStatusTrue();
    
    // Tìm promotion theo tên
    List<Promotion> findByNameContaining(String name);
    
    // Tìm promotion hợp lệ (active và trong thời gian)
    @Query("SELECT p FROM Promotion p WHERE p.status = true " +
           "AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(Date now);
}