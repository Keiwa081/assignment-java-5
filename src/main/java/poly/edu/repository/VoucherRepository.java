package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.model.Voucher;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeAndActiveTrue(String code);
}
