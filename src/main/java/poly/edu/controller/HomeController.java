
package poly.edu.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.model.Product;

@Controller
public class HomeController {
    
    @GetMapping("/home")
    public String home(Model model) {
        List<Product> featuredProducts = getFeaturedProducts();
        List<String> categories = getCategories();
        
        model.addAttribute("products", featuredProducts);
        model.addAttribute("categories", categories);
        
        return "poly/index";
    }
    
    @GetMapping("/under-construction")
    public String underConstruction() {
        return "poly/under-construction";
    }
    
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
            return "poly/productdesc";
        } else {
            return "poly/under-construction";
        }
    }
    
    private List<Product> getFeaturedProducts() {
        return Arrays.asList(
            Product.builder()
                .id(1L)
                .name("Laptop Dell XPS 13")
                .description("Laptop cao cấp, màn hình 13 inch")
                .price(25990000.0)
                .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=400")
                .rating(4.5)
                .category("Laptop")
                .build(),
            Product.builder()
                .id(2L)
                .name("iPhone 15 Pro")
                .description("Điện thoại thông minh mới nhất")
                .price(29990000.0)
                .imageUrl("https://www.apple.com/newsroom/images/2023/09/apple-unveils-iphone-15-pro-and-iphone-15-pro-max/article/Apple-iPhone-15-Pro-lineup-hero-230912_Full-Bleed-Image.jpg.xlarge.jpg")
                .rating(4.8)
                .category("Điện thoại")
                .build(),
            Product.builder()
                .id(3L)
                .name("Sony WH-1000XM5")
                .description("Tai nghe chống ồn cao cấp")
                .price(8990000.0)
                .imageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=400")
                .rating(4.7)
                .category("Tai nghe")
                .build(),
            Product.builder()
                .id(4L)
                .name("Samsung Galaxy Watch 6")
                .description("Đồng hồ thông minh")
                .price(6990000.0)
                .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400")
                .rating(4.4)
                .category("Đồng hồ")
                .build(),
            Product.builder()
                .id(5L)
                .name("iPad Pro M2")
                .description("Máy tính bảng cao cấp")
                .price(24990000.0)
                .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400")
                .rating(4.6)
                .category("Máy tính bảng")
                .build(),
            Product.builder()
                .id(6L)
                .name("Canon EOS R6")
                .description("Máy ảnh mirrorless chuyên nghiệp")
                .price(55990000.0)
                .imageUrl("https://bizweb.dktcdn.net/100/378/894/files/r5-vs-r6-2.jpg?v=1594623430159")
                .rating(4.9)
                .category("Máy ảnh")
                .build()
        );
    }
    
    private List<String> getCategories() {
        return Arrays.asList(
            "Điện thoại", "Laptop", "Máy tính bảng", 
            "Tai Nghe", "Máy ảnh", "Đồng hồ", "Chuột"
        );
    }
    
    private Product getProductById(Long id) {
        return getFeaturedProducts().stream()
            .filter(product -> product.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
        @GetMapping("/category/{name}")
    public String categoryPage(@PathVariable String name, Model model) {
    List<Product> featuredProducts = getFeaturedProducts();
    
    List<Product> filteredProducts = featuredProducts.stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(name))
            .toList();

    model.addAttribute("products", filteredProducts);
    model.addAttribute("categories", getCategories());
    model.addAttribute("categoryName", name);

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
    public String search(@RequestParam("q") String keyword, Model model) {
        List<Product> featuredProducts = getFeaturedProducts();

        // Lọc sản phẩm theo tên có chứa keyword (không phân biệt hoa thường)
        List<Product> searchResults = featuredProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        model.addAttribute("products", searchResults);
        model.addAttribute("categories", getCategories());
        model.addAttribute("keyword", keyword);

        return "poly/search"; // trang search.html
    }


}