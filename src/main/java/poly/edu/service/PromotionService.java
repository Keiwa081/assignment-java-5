package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.model.Promotion;
import poly.edu.repository.PromotionRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    // Lấy tất cả promotion
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }
    
    // Lấy promotion theo ID
    public Optional<Promotion> getPromotionById(Integer id) {
        return promotionRepository.findById(id);
    }
    
    // Lấy promotion đang active
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findByStatusTrue();
    }
    
    // Lấy promotion hợp lệ (active và trong thời gian)
    public List<Promotion> getValidPromotions() {
        return promotionRepository.findActivePromotions(new Date());
    }
    
    // Tạo promotion mới
    public Promotion createPromotion(Promotion promotion) {
        if (promotion.getStatus() == null) {
            promotion.setStatus(true);
        }
        return promotionRepository.save(promotion);
    }
    
    // Cập nhật promotion
    public Promotion updatePromotion(Integer id, Promotion promotion) {
        Optional<Promotion> existing = promotionRepository.findById(id);
        if (existing.isPresent()) {
            Promotion p = existing.get();
            p.setName(promotion.getName());
            p.setDescription(promotion.getDescription());
            p.setDiscount(promotion.getDiscount());
            p.setStartDate(promotion.getStartDate());
            p.setEndDate(promotion.getEndDate());
            p.setStatus(promotion.getStatus());
            return promotionRepository.save(p);
        }
        return null;
    }
    
    // Xóa promotion
    public boolean deletePromotion(Integer id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Validate promotion còn hợp lệ không
    public boolean isPromotionValid(Promotion promotion) {
        if (promotion == null || !promotion.getStatus()) return false;
        
        Date now = new Date();
        if (promotion.getStartDate() != null && promotion.getStartDate().after(now)) return false;
        if (promotion.getEndDate() != null && promotion.getEndDate().before(now)) return false;
        
        return true;
    }
}