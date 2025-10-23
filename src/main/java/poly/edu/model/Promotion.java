package poly.edu.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "Name")
    private String name; // Tên chương trình khuyến mãi
    
    @Column(name = "Description")
    private String description; // Mô tả chương trình
    
    @Column(name = "discount")
    private Double discount; // Mức giảm giá (0.20 = giảm 20%, 0.50 = giảm 50%)
    
    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    private Date startDate; // Ngày bắt đầu
    
    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private Date endDate; // Ngày kết thúc
    
    @Column(name = "status")
    private Boolean status; // Trạng thái (True/False)
    
    // Relationship with Product
    @OneToMany(mappedBy = "promotion")
    private List<Product> products;
}