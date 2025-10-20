package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "`Order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;

    @Column(name = "AccountId")
    private Integer accountId;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate;

    @Column(name = "StatusId")
    private Integer statusId;

    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "ShippingAddress", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Note", columnDefinition = "TEXT")
    private String note;
}