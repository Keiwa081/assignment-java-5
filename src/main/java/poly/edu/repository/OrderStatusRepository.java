package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.model.OrderStatus;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {
}