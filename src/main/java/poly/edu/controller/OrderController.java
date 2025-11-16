package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Order;
import poly.edu.model.OrderDetail;
import poly.edu.service.AuthService;
import poly.edu.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public String ordersPage(Model model, RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập để xem đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        Integer accountId = authService.getAccountId();
        List<Order> orders = orderService.getOrdersByAccount(accountId);
        model.addAttribute("orders", orders);
        return "poly/orders";
    }
    
    @GetMapping("/{orderId}")
    public String orderDetailPage(@PathVariable Integer orderId, 
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        Integer currentAccountId = authService.getAccountId();
        
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
    
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId,
                             RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        Integer accountId = authService.getAccountId();
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
    
    @GetMapping("/{orderId}/track")
    public String trackOrder(@PathVariable Integer orderId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        Integer currentAccountId = authService.getAccountId();
        
        if (!order.getAccountId().equals(currentAccountId)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền theo dõi đơn hàng này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        return "poly/order-tracking";
    }
}