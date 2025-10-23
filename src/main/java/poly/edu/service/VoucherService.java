package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.model.Voucher;
import poly.edu.repository.VoucherRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository repo;

    public Optional<Voucher> validateVoucher(String code) {
        Optional<Voucher> opt = repo.findByCodeAndActiveTrue(code);
        if (opt.isPresent()) {
            Voucher v = opt.get();
            Date now = new Date();
            if (v.getStartDate() != null && v.getStartDate().after(now)) return Optional.empty();
            if (v.getEndDate() != null && v.getEndDate().before(now)) return Optional.empty();
            if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) return Optional.empty();
            return Optional.of(v);
        }
        return Optional.empty();
    }

    public void markUsed(Voucher v) {
        v.setUsedCount(v.getUsedCount() + 1);
        repo.save(v);
    }
}
