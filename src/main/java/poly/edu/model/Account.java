package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    private Integer accountId;

    @Column(name = "Username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "Email", unique = true, nullable = false, length = 200)
    private String email;

    @Column(name = "Password", nullable = false, length = 100)
    private String password;

    @Column(name = "FullName", length = 200)
    private String fullName;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "Active")
    private Boolean active = true;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}