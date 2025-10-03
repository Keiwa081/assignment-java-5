package poly.edu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 64)
    private String password;

    private Boolean sex;

    private LocalDate birth;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    private String fullName;

    private Boolean activated;
}
