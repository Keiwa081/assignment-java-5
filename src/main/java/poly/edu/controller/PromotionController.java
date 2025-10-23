package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.model.Account;
import poly.edu.model.Promotion;
import poly.edu.service.PromotionService;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/promotion")
public class PromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    /**
     * Check if current user is admin
     */
    private boolean isAdmin(HttpSession session) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) return false;
        
        return account.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getRoleName()));
    }
    
    /**
     * Danh sách promotion
     */
    @GetMapping
    public String listPromotions(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        List<Promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions", promotions);
        
        return "poly/admin/promotion_list";
    }
    
    /**
     * Form thêm promotion
     */
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        model.addAttribute("promotion", new Promotion());
        return "poly/admin/promotion_add";
    }
    
    /**
     * Lưu promotion mới
     */
    @PostMapping("/save")
    public String savePromotion(@RequestParam String name,
                               @RequestParam String description,
                               @RequestParam Double discount,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                               @RequestParam(defaultValue = "true") Boolean status,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            // Validate
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Tên chương trình không được để trống!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/promotion/add";
            }
            
            if (discount == null || discount < 0 || discount > 1) {
                redirectAttributes.addFlashAttribute("message", "❌ Mức giảm giá phải từ 0 đến 1 (0% - 100%)!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/promotion/add";
            }
            
            if (startDate.after(endDate)) {
                redirectAttributes.addFlashAttribute("message", "❌ Ngày bắt đầu phải trước ngày kết thúc!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/promotion/add";
            }
            
            Promotion promotion = new Promotion();
            promotion.setName(name);
            promotion.setDescription(description);
            promotion.setDiscount(discount);
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setStatus(status);
            
            promotionService.createPromotion(promotion);
            
            redirectAttributes.addFlashAttribute("message", "✅ Thêm chương trình khuyến mãi thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
            return "redirect:/admin/promotion";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/promotion/add";
        }
    }
    
    /**
     * Form sửa promotion
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        Promotion promotion = promotionService.getPromotionById(id).orElse(null);
        
        if (promotion == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy chương trình khuyến mãi!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/promotion";
        }
        
        model.addAttribute("promotion", promotion);
        return "poly/admin/promotion_edit";
    }
    
    /**
     * Cập nhật promotion
     */
    @PostMapping("/update/{id}")
    public String updatePromotion(@PathVariable Integer id,
                                 @RequestParam String name,
                                 @RequestParam String description,
                                 @RequestParam Double discount,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                 @RequestParam(defaultValue = "true") Boolean status,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            Promotion promotion = new Promotion();
            promotion.setName(name);
            promotion.setDescription(description);
            promotion.setDiscount(discount);
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setStatus(status);
            
            Promotion updated = promotionService.updatePromotion(id, promotion);
            
            if (updated != null) {
                redirectAttributes.addFlashAttribute("message", "✅ Cập nhật chương trình khuyến mãi thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy chương trình khuyến mãi!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
            
            return "redirect:/admin/promotion";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/promotion/edit/" + id;
        }
    }
    
    /**
     * Xóa promotion
     */
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            boolean deleted = promotionService.deletePromotion(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("message", "✅ Xóa chương trình khuyến mãi thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy chương trình khuyến mãi!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Không thể xóa: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/admin/promotion";
    }
}