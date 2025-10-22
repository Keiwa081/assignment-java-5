package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    // ✅ Hiển thị form thêm sản phẩm
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());
        return "poly/admin/product_add";
    }

    // ✅ Lưu sản phẩm mới
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        product.setCreatedAt(LocalDateTime.now());
        productRepo.save(product);

        return "redirect:/account"; // Sau khi lưu, quay về trang account
    }
}
