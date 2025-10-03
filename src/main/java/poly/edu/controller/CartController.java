package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import poly.edu.model.Product;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cartItems")
public class CartController {
    
    // Get all available products (same as HomeController)
    private List<Product> getAllProducts() {
        return List.of(
            Product.builder()
                .id(1L)
                .name("Laptop Dell XPS 13")
                .description("Laptop cao cấp, màn hình 13 inch")
                .price(25990000.0)
                .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=400")
                .rating(4.5)
                .build(),
            Product.builder()
                .id(2L)
                .name("iPhone 15 Pro")
                .description("Điện thoại thông minh mới nhất")
                .price(29990000.0)
                .imageUrl("https://www.apple.com/newsroom/images/2023/09/apple-unveils-iphone-15-pro-and-iphone-15-pro-max/article/Apple-iPhone-15-Pro-lineup-hero-230912_Full-Bleed-Image.jpg.xlarge.jpg")
                .rating(4.8)
                .build(),
            Product.builder()
                .id(3L)
                .name("Sony WH-1000XM5")
                .description("Tai nghe chống ồn cao cấp")
                .price(8990000.0)
                .imageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=400")
                .rating(4.7)
                .build(),
            Product.builder()
                .id(4L)
                .name("Samsung Galaxy Watch 6")
                .description("Đồng hồ thông minh")
                .price(6990000.0)
                .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400")
                .rating(4.4)
                .build(),
            Product.builder()
                .id(5L)
                .name("iPad Pro M2")
                .description("Máy tính bảng cao cấp")
                .price(24990000.0)
                .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400")
                .rating(4.6)
                .build(),
            Product.builder()
                .id(6L)
                .name("Canon EOS R6")
                .description("Máy ảnh mirrorless chuyên nghiệp")
                .price(55990000.0)
                .imageUrl("https://bizweb.dktcdn.net/100/378/894/files/r5-vs-r6-2.jpg?v=1594623430159")
                .rating(4.9)
                .build()
        );
    }
    
    @GetMapping
    public String cartPage(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }
        
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("itemCount", cartItems.size());
        
        return "poly/cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, 
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session) {
        
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }
        
        // Find the product
        Optional<Product> productOpt = getAllProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst();
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // Check if item already exists in cart
            Optional<CartItem> existingItem = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
            } else {
                // Add new item
                cartItems.add(new CartItem(product, quantity));
            }
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId, 
                                @RequestParam Integer quantity,
                                HttpSession session) {
        
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        
        if (cartItems != null) {
            cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(quantity));
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeItem(@RequestParam Long productId, HttpSession session) {
        
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        
        if (cartItems != null) {
            cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        
        if (cartItems != null) {
            cartItems.clear();
        }
        
        return "redirect:/cart";
    }
    
    // Inner class for cart items
    public static class CartItem {
        private Product product;
        private Integer quantity;
        
        public CartItem(Product product, Integer quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public void setProduct(Product product) {
            this.product = product;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public Double getSubtotal() {
            return product.getPrice() * quantity;
        }
    }
}
