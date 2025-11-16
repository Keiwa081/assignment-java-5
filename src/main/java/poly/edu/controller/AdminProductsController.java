package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.CategoryRepository;
import poly.edu.service.AuthService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProductsController {
    
    @Autowired
    private ProductRepository productRepo;
    
    @Autowired
    private CategoryRepository categoryRepo;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Hiển thị danh sách sản phẩm
     * URL: /admin/products
     */
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String search,
                              @RequestParam(required = false) Integer category,
                              @RequestParam(required = false) String stock,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            return "redirect:/account/login";
        }
        
        if (!authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/home";
        }
        
        try {
            List<Product> allProducts = productRepo.findAll();
            List<Product> filteredProducts = allProducts;
            
            // Lọc theo tìm kiếm
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                               (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
            }
            
            // Lọc theo danh mục
            if (category != null) {
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getCategoryId().equals(category))
                    .toList();
            }
            
            // Lọc theo tồn kho
            if (stock != null) {
                switch (stock) {
                    case "in":
                        filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getQuantity() >= 10)
                            .toList();
                        break;
                    case "low":
                        filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getQuantity() > 0 && p.getQuantity() < 10)
                            .toList();
                        break;
                    case "out":
                        filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getQuantity() == 0)
                            .toList();
                        break;
                }
            }
            
            model.addAttribute("products", filteredProducts);
            model.addAttribute("categories", categoryRepo.findAll());
            model.addAttribute("currentPage", page);
            
            return "poly/admin/products";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/dashboard";
        }
    }
}