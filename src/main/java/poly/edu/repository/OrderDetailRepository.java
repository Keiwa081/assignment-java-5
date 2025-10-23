package poly.edu.repository;

import poly.edu.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    /**
     * Find all order details by order ID
     */
    List<OrderDetail> findByOrderId(Integer orderId);

    /**
     * Find order details by product ID
     */
    List<OrderDetail> findByProductId(Integer productId);

    /**
     * Count total items in an order
     */
    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.orderId = :orderId")
    long countByOrderId(@Param("orderId") Integer orderId);

    /**
     * Get total quantity for an order
     */
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.orderId = :orderId")
    Integer getTotalQuantityByOrderId(@Param("orderId") Integer orderId);

    /**
     * Get total amount for an order
     * ✅ FIX: Đổi từ od.price thành od.unitPrice
     */
    @Query("SELECT COALESCE(SUM(od.unitPrice * od.quantity), 0) FROM OrderDetail od WHERE od.orderId = :orderId")
    Double getTotalAmountByOrderId(@Param("orderId") Integer orderId);

    /**
     * Delete all order details by order ID
     */
    void deleteByOrderId(Integer orderId);

    /**
     * Check if product exists in any order
     */
    boolean existsByProductId(Integer productId);
}