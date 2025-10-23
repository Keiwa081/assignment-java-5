
package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.model.Category;
import poly.edu.model.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/home")
    public String home(Model model, @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, 12);
            Page<Product> products = productRepository.findAll(pageable);

            // ✅ Lấy category từ database
            List<Category> categories = categoryService.getCategoriesWithProducts();

            model.addAttribute("products", products.getContent());
            model.addAttribute("categories", categories);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("totalProducts", products.getTotalElements());

            return "poly/index";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalProducts", 0);
            return "poly/index";
        }
    }

    
    @GetMapping("/under-construction")
    public String underConstruction() {
        return "poly/under-construction";
    }
    
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            List<Category> categories = categoryService.getCategoriesWithProducts();
            model.addAttribute("categories", categories);

            // ✅ Add product vào model
            model.addAttribute("product", product);

            return "poly/productdesc";
        } else {
            return "poly/under-construction";
        }
    }


    @GetMapping("/about")
    public String aboutPage() {
        return "poly/about";
    }
    
    @GetMapping("/test-db")
    public String testDatabase(Model model) {
        try {
            // Test database connection by getting all products
            List<Product> allProducts = productRepository.findAll();
            model.addAttribute("message", "Database connection successful! Found " + allProducts.size() + " products.");
            model.addAttribute("products", allProducts);
            return "poly/test-db";
        } catch (Exception e) {
            model.addAttribute("message", "Database connection failed: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
            return "poly/test-db";
        }
    }

    
    @GetMapping("/category/{name}")
    public String viewCategory(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Category category = categoryService.getCategoryByName(name)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Pageable pageable = PageRequest.of(page, 12);
        Page<Product> products = productRepository.findByCategoryId(category.getCategoryId(), pageable);

        
        List<Category> categories = categoryService.getCategoriesWithProducts();
        model.addAttribute("categories", categories);

        model.addAttribute("categoryName", category.getName());
        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());

        return "poly/category";
    }


        @GetMapping("/terms")
    public String terms() {
        return "poly/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "poly/privacy";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam("q") String keyword, Model model, @RequestParam(defaultValue = "0") int page) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Page<Product> featuredProducts = productService.getFeaturedProducts(page, 12);
            model.addAttribute("products", featuredProducts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", featuredProducts.getTotalPages());
            model.addAttribute("totalProducts", featuredProducts.getTotalElements());
        } else {
            Page<Product> searchResults = productService.searchProducts(keyword, page, 12);
            model.addAttribute("products", searchResults.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", searchResults.getTotalPages());
            model.addAttribute("totalProducts", searchResults.getTotalElements());
        }

        List<Category> categories = categoryService.getCategoriesWithProducts();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);

        return "poly/search";
    }



}