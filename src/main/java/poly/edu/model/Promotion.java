package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "discount", nullable = false) // Khớp với database
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "startDate", nullable = false) // Khớp với database
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false) // Khớp với database
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean status = true;
}