package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountId")
    private int accountId;

    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "Password", nullable = false, length = 50)
    private String password;

    @Column(name = "FullName", length = 200)
    private String fullName;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Address", length = 500)
    private String address;

    @Column(name = "Active", nullable = false)
    private Boolean active;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ResetCode", length = 20)
    private String resetCode;

    // ✅ Thêm phần này
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "AccountRole",
        joinColumns = @JoinColumn(name = "AccountId"),
        inverseJoinColumns = @JoinColumn(name = "RoleId")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();
}
