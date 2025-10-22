package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Integer cartId;

    @Column(name = "AccountId", nullable = false)
    private Integer accountId;

    @Column(name = "ProductId", nullable = false)
    private Integer productId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "AddedAt", nullable = false)
    private LocalDateTime addedAt;

    // Relationship with Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", insertable = false, updatable = false)
    private Product product;

    // Relationship with Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId", insertable = false, updatable = false)
    private Account account;
}



