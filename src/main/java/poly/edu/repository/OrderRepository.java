package poly.edu.repository;

import poly.edu.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Find orders by account ID (for users)
    List<Order> findByAccountIdOrderByOrderDateDesc(Integer accountId);
    
    // Find all orders ordered by date (for admin)
    List<Order> findAllByOrderByOrderDateDesc();
    
    // Find orders by StatusId
    List<Order> findByStatusIdOrderByOrderDateDesc(Integer statusId);
    
    // Find orders by account and StatusId
    List<Order> findByAccountIdAndStatusIdOrderByOrderDateDesc(Integer accountId, Integer statusId);
    
    // Count orders by account
    @Query("SELECT COUNT(o) FROM Order o WHERE o.accountId = :accountId")
    long countByAccountId(@Param("accountId") Integer accountId);
    
    // Get total amount by account
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.accountId = :accountId")
    Double getTotalAmountByAccountId(@Param("accountId") Integer accountId);
    
    // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.statusId = :statusId")
    long countByStatusId(@Param("statusId") Integer statusId);
    
    // Get total revenue
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.statusId = 4")
    Double getTotalRevenue();
    
    // Get orders in date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                      @Param("endDate") java.time.LocalDateTime endDate);
}