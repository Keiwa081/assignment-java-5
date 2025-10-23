package poly.edu.repository;

import poly.edu.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    // Find cart items by account ID
    List<Cart> findByAccountId(Integer accountId);

    // Find cart item by account and product
    Optional<Cart> findByAccountIdAndProductId(Integer accountId, Integer productId);

    // Delete cart items by account ID
    @Transactional
    @Modifying
    void deleteByAccountId(Integer accountId);

    // Delete specific cart item by account and product
    @Transactional
    @Modifying
    void deleteByAccountIdAndProductId(Integer accountId, Integer productId);

    // Update quantity for specific cart item
    @Transactional
    @Modifying
    @Query("UPDATE Cart c SET c.quantity = :quantity WHERE c.accountId = :accountId AND c.productId = :productId")
    int updateQuantity(@Param("accountId") Integer accountId, 
                      @Param("productId") Integer productId, 
                      @Param("quantity") Integer quantity);

    // Check if product exists in cart
    boolean existsByAccountIdAndProductId(Integer accountId, Integer productId);

    // Count total items in cart for account
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.accountId = :accountId")
    long countByAccountId(@Param("accountId") Integer accountId);

    // Get total quantity in cart for account
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM Cart c WHERE c.accountId = :accountId")
    Integer getTotalQuantityByAccountId(@Param("accountId") Integer accountId);
}