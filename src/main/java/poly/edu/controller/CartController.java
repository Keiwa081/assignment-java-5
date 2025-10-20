package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import poly.edu.model.Product;
import poly.edu.repository.ProductRepository;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cartItems")
public class CartController {

    @Autowired
    private ProductRepository productRepository;

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
                            @RequestParam(required = false) String from,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }

        // Lấy sản phẩm từ database
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Kiểm tra sản phẩm đã có trong giỏ chưa
            Optional<CartItem> existingItem = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Cập nhật số lượng
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                redirectAttributes.addFlashAttribute("message", "✅ Đã cập nhật số lượng " + product.getName() + " trong giỏ hàng! (Tổng: " + item.getQuantity() + " sản phẩm)");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                // Thêm sản phẩm mới
                cartItems.add(new CartItem(product, quantity));
                redirectAttributes.addFlashAttribute("message", "✅ Đã thêm " + product.getName() + " vào giỏ hàng thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/cart";
    }

    @GetMapping("/debug")
    public String debugCart(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

        model.addAttribute("cartItems", cartItems != null ? cartItems : new ArrayList<>());
        model.addAttribute("sessionId", session.getId());

        return "poly/cart";
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

    // Inner class cho cart items
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