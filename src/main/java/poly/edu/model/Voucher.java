package poly.edu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code; // Mã voucher, ví dụ "SALE10"

    private Boolean active = true; // Có đang hoạt động không

    private Double discountPercent; // Phần trăm giảm, ví dụ: 10 = giảm 10%

    private Integer usageLimit; // Số lần được sử dụng tối đa
    private Integer usedCount = 0; // Số lần đã sử dụng

    @Temporal(TemporalType.DATE)
    private Date startDate; // Ngày bắt đầu hiệu lực

    @Temporal(TemporalType.DATE)
    private Date endDate; // Ngày kết thúc
}
