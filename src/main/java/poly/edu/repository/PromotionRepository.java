package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}