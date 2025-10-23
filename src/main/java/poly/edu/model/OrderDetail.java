package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OrderDetail")
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderDetailId")
    private Integer orderDetailId;
    
    @Column(name = "OrderId", nullable = false)
    private Integer orderId;
    
    @Column(name = "ProductId", nullable = false)
    private Integer productId;
    
    @Column(name = "Quantity", nullable = false)
    private Integer quantity;
    
    // ✅ FIX: Đổi tên field thành unitPrice (khớp với tên column)
    @Column(name = "UnitPrice", nullable = false)
    private Double unitPrice;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", insertable = false, updatable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", insertable = false, updatable = false)
    private Product product;
    
    // ✅ BONUS: Computed field cho Price (UnitPrice * Quantity)
    @Transient
    public Double getPrice() {
        return this.unitPrice != null && this.quantity != null 
            ? this.unitPrice * this.quantity 
            : 0.0;
    }
}