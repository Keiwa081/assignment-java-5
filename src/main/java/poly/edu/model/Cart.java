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

    @Column(name = "AccountId")
    private Integer accountId;

    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "AddedAt")
    private LocalDateTime addedAt;
}