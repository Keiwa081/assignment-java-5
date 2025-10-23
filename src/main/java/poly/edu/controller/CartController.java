package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;
import poly.edu.model.Cart;
import poly.edu.service.CartService;
import poly.edu.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AccountDAO accountDAO;
    
    // ==================== HELPER METHODS ====================
    
    private Account getCurrentAccount(HttpSession session) {
        return (Account) session.getAttribute("account");
    }
    
    private Integer getCurrentAccountId(HttpSession session) {
        Account account = getCurrentAccount(session);
        return account != null ? account.getAccountId() : null;
    }
    
    private boolean isLoggedIn(HttpSession session) {
        return getCurrentAccount(session) != null;
    }
    
    // ==================== CART DISPLAY ====================
    
    @GetMapping
    public String cartPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để xem giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        List<Cart> cartItems = cartService.getCartItems(accountId);
        
        // ✅ FIX: Tính tổng tiền với giá đã giảm
        double total = cartItems.stream()
                .mapToDouble(item -> {
                    // Sử dụng giá đã giảm thay vì giá gốc
                    Double price = item.getProduct().getDiscountedPrice();
                    return price * item.getQuantity();
                })
                .sum();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("itemCount", cartItems.size());
        
        return "poly/cart";
    }
    
    // ==================== CART OPERATIONS ====================
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId, 
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @RequestParam(required = false) String from,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        String result = cartService.addToCart(accountId, productId, quantity);
        
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
        
        if ("product".equals(from)) {
            return "redirect:/product/" + productId;
        } else {
            return "redirect:/cart";
        }
    }
    
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Integer productId, 
                                @RequestParam Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        String result = cartService.updateCartItemQuantity(accountId, productId, quantity);
        
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
    public String removeItem(@RequestParam Integer productId, 
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        boolean success = cartService.removeFromCart(accountId, productId);
        
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
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        boolean success = cartService.clearCart(accountId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã xóa tất cả sản phẩm khỏi giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể xóa giỏ hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/cart";
    }

    // ==================== CHECKOUT FUNCTIONALITY ====================
    
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để thanh toán!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        List<Cart> cartItems = cartService.getCartItems(accountId);
        
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "❌ Giỏ hàng của bạn đang trống!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart";
        }
        
        // ✅ FIX: Tính tổng tiền với giá đã giảm
        double total = cartItems.stream()
                .mapToDouble(item -> {
                    Double price = item.getProduct().getDiscountedPrice();
                    return price * item.getQuantity();
                })
                .sum();
        
        Account account = getCurrentAccount(session);
        
        if (account.getPhone() == null || account.getAddress() == null) {
            account = accountDAO.findById(accountId).orElse(account);
        }
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("account", account);
        
        return "poly/checkout";
    }
    
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam String shippingAddress,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String note,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để đặt hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng nhập địa chỉ giao hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng nhập số điện thoại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        }
        
        String result = orderService.createOrderFromCart(accountId, shippingAddress, phone, note);
        
        if (result.equals("empty_cart")) {
            redirectAttributes.addFlashAttribute("message", "❌ Giỏ hàng của bạn đang trống!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart";
        } else if (result.startsWith("out_of_stock:")) {
            String productName = result.split(":")[1];
            redirectAttributes.addFlashAttribute("message", "❌ Sản phẩm '" + productName + "' đã hết hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        } else if (result.startsWith("insufficient_stock:")) {
            String[] parts = result.split(":");
            String productName = parts[1];
            String availableQty = parts[2];
            redirectAttributes.addFlashAttribute("message", "❌ Sản phẩm '" + productName + "' chỉ còn " + availableQty + " sản phẩm trong kho!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        } else if (result.startsWith("product_not_found:")) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy sản phẩm!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        } else if (result.startsWith("success:")) {
            String orderId = result.split(":")[1];
            redirectAttributes.addFlashAttribute("message", "✅ Đặt hàng thành công! Mã đơn hàng: #" + orderId);
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/orders/" + orderId;
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Đã có lỗi xảy ra khi đặt hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/cart/checkout";
        }
    }
}