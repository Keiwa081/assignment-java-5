package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Account;
import poly.edu.model.Product;
import poly.edu.model.Category;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/product")	
public class ProductController {
    
    @Autowired
    private ProductRepository productRepo;
    
    @Autowired
    private CategoryRepository categoryRepo;
    
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
     * Hiển thị form thêm sản phẩm
     */
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        // Check if user is admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());
        return "poly/admin/product_add";
    }
    
    /**
     * Lưu sản phẩm mới
     */
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        // Check admin permission
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            // Validate input
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Tên sản phẩm không được để trống!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/product/add";
            }
            
            if (product.getPrice() == null || product.getPrice() <= 0) {
                redirectAttributes.addFlashAttribute("message", "❌ Giá sản phẩm phải lớn hơn 0!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/product/add";
            }
            
            if (product.getQuantity() == null || product.getQuantity() < 0) {
                redirectAttributes.addFlashAttribute("message", "❌ Số lượng sản phẩm không hợp lệ!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/product/add";
            }
            
            if (product.getCategoryId() == null) {
                redirectAttributes.addFlashAttribute("message", "❌ Vui lòng chọn danh mục!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/product/add";
            }
            
            // Set default values
            if (product.getRating() == null) {
                product.setRating(0.0);
            }
            
            product.setCreatedAt(LocalDateTime.now());
            
            // TODO: Handle image file upload if needed
            // if (imageFile != null && !imageFile.isEmpty()) {
            //     String imageUrl = uploadImage(imageFile);
            //     product.setImageUrl(imageUrl);
            // }
            
            productRepo.save(product);
            
            redirectAttributes.addFlashAttribute("message", "✅ Thêm sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
            // Redirect to dashboard after saving
            return "redirect:/admin/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/product/add";
        }
    }
    
    /**
     * Hiển thị form sửa sản phẩm
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Check admin permission
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        Product product = productRepo.findById(id).orElse(null);
        
        if (product == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/dashboard";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        
        return "poly/admin/product_edit";
    }
    
    /**
     * Cập nhật sản phẩm
     */
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Integer id,
                               @ModelAttribute Product product,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        // Check admin permission
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            Product existingProduct = productRepo.findById(id).orElse(null);
            
            if (existingProduct == null) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/dashboard";
            }
            
            // Update product fields
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setQuantity(product.getQuantity());
            existingProduct.setImageUrl(product.getImageUrl());
            existingProduct.setCategoryId(product.getCategoryId());
            
            if (product.getRating() != null) {
                existingProduct.setRating(product.getRating());
            }
            
            // TODO: Handle image file upload if needed
            
            productRepo.save(existingProduct);
            
            redirectAttributes.addFlashAttribute("message", "✅ Cập nhật sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
            return "redirect:/admin/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/product/edit/" + id;
        }
    }
    
    /**
     * Xóa sản phẩm
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Check admin permission
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền thực hiện thao tác này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            Product product = productRepo.findById(id).orElse(null);
            
            if (product == null) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/dashboard";
            }
            
            productRepo.delete(product);
            
            redirectAttributes.addFlashAttribute("message", "✅ Xóa sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Không thể xóa sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/admin/dashboard";
    }
}