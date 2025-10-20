package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.model.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.CategoryRepository;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @ModelAttribute("categories")
    public List<String> getCategories() {
        try {
            List<String> categories = categoryRepository.findAllCategoryNames();
            System.out.println("Categories lấy được: " + categories.size());
            return categories;
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy categories: " + e.getMessage());
            e.printStackTrace();
            return List.of("Điện thoại", "Laptop", "Máy tính bảng");
        }
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        try {
            System.out.println("=== BẮT ĐẦU LẤY SẢN PHẨM ===");

            // Kiểm tra repository
            System.out.println("ProductRepository: " + (productRepository != null ? "OK" : "NULL"));

            // Kiểm tra tổng số sản phẩm
            long total = productRepository.count();
            System.out.println("Tổng số sản phẩm: " + total);

            // Lấy sản phẩm
            List<Product> featuredProducts = productRepository.findTop9Products();
            System.out.println("Sản phẩm lấy được: " + featuredProducts.size());

            // Debug từng sản phẩm
            for (Product p : featuredProducts) {
                System.out.println("Product: " + p.getId() + " - " + p.getName() + " - " + p.getPrice());
            }

            model.addAttribute("products", featuredProducts);
            return "poly/index";

        } catch (Exception e) {
            System.out.println("LỖI NGHIÊM TRỌNG: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("products", List.of());
            return "poly/index";
        }
    }
}

//    @GetMapping("/product/{id}")
//    public String productDetail(@PathVariable Long id, Model model) {
//        try {
//            Product product = productRepository.findById(id)
//                    .orElse(null);
//
//            if (product != null) {
//                model.addAttribute("product", product);
//                return "poly/productdesc";
//            } else {
//                return "poly/under-construction";
//            }
//        } catch (Exception e) {
//            System.out.println("Lỗi product detail: " + e.getMessage());
//            return "poly/under-construction";
//        }
//    }
//
//    @GetMapping("/category/{name}")
//    public String categoryPage(@PathVariable String name, Model model) {
//        try {
//            // Tìm category theo tên
//            var category = categoryRepository.findByName(name);
//
//            List<Product> filteredProducts;
//            if (category != null) {
//                // Lấy sản phẩm theo categoryId
//                filteredProducts = productRepository.findByCategoryId(category.getId());
//            } else {
//                filteredProducts = List.of();
//            }
//
//            model.addAttribute("products", filteredProducts);
//            model.addAttribute("categoryName", name);
//            return "poly/category";
//
//        } catch (Exception e) {
//            System.out.println("Lỗi category page: " + e.getMessage());
//            model.addAttribute("products", List.of());
//            model.addAttribute("categoryName", name);
//            return "poly/category";
//        }
//    }
//
//    @GetMapping("/search")
//    public String search(@RequestParam("q") String keyword, Model model) {
//        try {
//            List<Product> searchResults = productRepository.findByNameContainingIgnoreCase(keyword);
//            model.addAttribute("products", searchResults);
//            model.addAttribute("keyword", keyword);
//            return "poly/search";
//        } catch (Exception e) {
//            System.out.println("Lỗi search: " + e.getMessage());
//            model.addAttribute("products", List.of());
//            model.addAttribute("keyword", keyword);
//            return "poly/search";
//        }
//    }
//
//    // Các method khác giữ nguyên...
//    @GetMapping("/under-construction")
//    public String underConstruction() {
//        return "poly/under-construction";
//    }
//
//    @GetMapping("/about")
//    public String aboutPage() {
//        return "poly/about";
//    }
//
//    @GetMapping("/terms")
//    public String terms() {
//        return "poly/terms";
//    }
//
//    @GetMapping("/privacy")
//    public String privacy() {
//        return "poly/privacy";
//    }
