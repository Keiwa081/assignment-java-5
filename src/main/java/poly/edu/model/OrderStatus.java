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
@Table(name = "OrderStatus")
public class OrderStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StatusId")
    private Integer statusId;
    
    @Column(name = "StatusName", nullable = false)
    private String statusName;
    
    @Column(name = "Description", nullable = true)
    private String description;
    
}