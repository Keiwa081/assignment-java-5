// ==================== OrderController.java ====================
// Replace your existing OrderController with this updated version

package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Account;
import poly.edu.model.Order;
import poly.edu.model.OrderDetail;
import poly.edu.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Helper method to get current logged-in account ID
     */
    private Integer getCurrentAccountId(HttpSession session) {
        Account account = (Account) session.getAttribute("account");
        return account != null ? account.getAccountId() : null;
    }
    
    /**
     * Check if user is logged in
     */
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("account") != null;
    }
    
    /**
     * Display all orders for current user
     */
    @GetMapping
    public String ordersPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để xem đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        List<Order> orders = orderService.getOrdersByAccount(accountId);
        model.addAttribute("orders", orders);
        return "poly/orders";
    }
    
    /**
     * Display order detail
     */
    @GetMapping("/{orderId}")
    public String orderDetailPage(@PathVariable Integer orderId, 
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        Integer currentAccountId = getCurrentAccountId(session);
        
        // Check if order belongs to current user
        if (!order.getAccountId().equals(currentAccountId)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền xem đơn hàng này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        List<OrderDetail> orderDetails = orderService.getOrderDetails(orderId);
        
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        
        return "poly/order-detail";
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Integer accountId = getCurrentAccountId(session);
        boolean success = orderService.cancelOrder(orderId, accountId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "✅ Đã hủy đơn hàng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể hủy đơn hàng! Chỉ có thể hủy đơn hàng đang ở trạng thái 'Chờ xử lý'.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/orders/" + orderId;
    }
    
    /**
     * Track order status
     */
    @GetMapping("/{orderId}/track")
    public String trackOrder(@PathVariable Integer orderId,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        Integer currentAccountId = getCurrentAccountId(session);
        
        if (!order.getAccountId().equals(currentAccountId)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền theo dõi đơn hàng này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        return "poly/order-tracking";
    }
}