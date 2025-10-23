package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "[Order]")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;
    
    @Column(name = "AccountId", nullable = false)
    private Integer accountId;
    
    @Column(name = "OrderDate", nullable = false)
    private LocalDateTime orderDate;
    
    @Column(name = "StatusId", nullable = false)
    @Builder.Default
    private Integer statusId = 1; // ✅ Mặc định là 1 (Pending/Chờ xử lý)
    
    @Column(name = "Total")
    private Double total;
    
    @Column(name = "ShippingAddress")
    private String shippingAddress;
    
    @Column(name = "Phone")
    private String phone;
    
    @Column(name = "Note")
    private String note;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId", insertable = false, updatable = false)
    private Account account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StatusId", insertable = false, updatable = false)
    private OrderStatus orderStatus;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
    
    // ✅ Helper method để lấy tên trạng thái
    @Transient
    public String getStatusName() {
        if (orderStatus != null) {
            return orderStatus.getStatusName();
        }
        // Fallback nếu chưa load relationship
        return getStatusNameById(this.statusId);
    }
    
    // ✅ Static helper để map statusId sang tên (dùng cho view)
    @Transient
    public String getStatus() {
        return getStatusNameById(this.statusId);
    }
    
    private static String getStatusNameById(Integer statusId) {
        if (statusId == null) return "Unknown";
        switch (statusId) {
            case 1: return "Pending";
            case 2: return "Processing";
            case 3: return "Shipped";
            case 4: return "Delivered";
            case 5: return "Cancelled";
            default: return "Unknown";
        }
    }
    
    // ✅ Method kiểm tra có thể hủy đơn không (chỉ khi statusId = 1)
    @Transient
    public boolean isCancellable() {
        return this.statusId != null && this.statusId == 1;
    }
    
    // ✅ Method kiểm tra đơn đã hoàn thành
    @Transient
    public boolean isCompleted() {
        return this.statusId != null && this.statusId == 4;
    }
    
    // ✅ Method kiểm tra đơn đã bị hủy
    @Transient
    public boolean isCancelled() {
        return this.statusId != null && this.statusId == 5;
    }
}