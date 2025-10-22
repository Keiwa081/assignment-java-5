package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Integer productId;

    @Column(name = "CategoryId", nullable = false)
    private Integer categoryId;

    @Column(name = "Name", nullable = false, length = 300)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "ImageUrl", length = 1000)
    private String imageUrl;

    @Column(name = "Rating")
    private Double rating;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    // ✅ Tránh vòng lặp vô hạn trong toString() và equals/hashCode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", insertable = false, updatable = false)
    private Category category;
}
