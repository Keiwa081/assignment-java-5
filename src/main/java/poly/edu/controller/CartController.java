package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Cart;
import poly.edu.service.CartService;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    // For now, we'll use a default account ID (1) since we don't have authentication yet
    private static final Integer DEFAULT_ACCOUNT_ID = 1;
    
    @GetMapping
    public String cartPage(Model model) {
        List<Cart> cartItems = cartService.getCartItems(DEFAULT_ACCOUNT_ID);
        
        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("itemCount", cartItems.size());
        
        return "poly/cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId, 
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @RequestParam(required = false) String from,
                           RedirectAttributes redirectAttributes) {
        
        String result = cartService.addToCart(DEFAULT_ACCOUNT_ID, productId, quantity);
        
        if (result.equals("out_of_stock")) {
            redirectAttributes.addFlashAttribute("message", "❌ Sản phẩm đã hết hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } else if (result.startsWith("insufficient_stock:")) {
            String availableQty = result.split(":")[1];
            redirectAttributes.addFlashAttribute("message", "❌ Chỉ còn " + availableQty + " sản phẩm trong kho!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } else if (result.startsWith("updated:")) {
            String newQty = result.split(":")[1];
            redirectAttributes.addFlashAttribute("message", "✅ Đã cập nhật số lượng trong giỏ hàng! (Tổng: " + newQty + " sản phẩm)");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else if (result.startsWith("added:")) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã thêm sản phẩm vào giỏ hàng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể thêm sản phẩm vào giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        // Redirect based on where the request came from
        if ("product".equals(from)) {
            return "redirect:/product/" + productId;
        } else {
            return "redirect:/cart";
        }
    }
    
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Integer productId, 
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        
        String result = cartService.updateCartItemQuantity(DEFAULT_ACCOUNT_ID, productId, quantity);
        
        if (result.equals("removed")) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã xóa sản phẩm khỏi giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else if (result.startsWith("insufficient_stock:")) {
            String availableQty = result.split(":")[1];
            redirectAttributes.addFlashAttribute("message", "❌ Chỉ còn " + availableQty + " sản phẩm trong kho!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } else if (result.startsWith("updated:")) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã cập nhật số lượng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else if (result.equals("not_found")) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm trong giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeItem(@RequestParam Integer productId, RedirectAttributes redirectAttributes) {
        boolean success = cartService.removeFromCart(DEFAULT_ACCOUNT_ID, productId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã xóa sản phẩm khỏi giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể xóa sản phẩm!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        boolean success = cartService.clearCart(DEFAULT_ACCOUNT_ID);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã xóa tất cả sản phẩm khỏi giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể xóa giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/cart";
    }
    
}
