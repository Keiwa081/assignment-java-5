package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.model.Order;
import poly.edu.model.OrderDetail;
import poly.edu.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Display all orders (Admin view)
     */
    @GetMapping
    public String adminOrdersPage(Model model, 
                                 @RequestParam(required = false) Integer statusFilter) {
        List<Order> orders;
        
        if (statusFilter != null && statusFilter > 0) {
            orders = orderService.getOrdersByStatusId(statusFilter);
        } else {
            orders = orderService.getAllOrders();
        }
        
        // Calculate statistics
        long pendingCount = orders.stream().filter(o -> o.getStatusId() == 1).count();
        long processingCount = orders.stream().filter(o -> o.getStatusId() == 2).count();
        long shippedCount = orders.stream().filter(o -> o.getStatusId() == 3).count();
        long deliveredCount = orders.stream().filter(o -> o.getStatusId() == 4).count();
        
        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("deliveredCount", deliveredCount);
        
        return "poly/admin/orders";
    }
    
    /**
     * Display order detail (Admin view)
     */
    @GetMapping("/{orderId}")
    public String adminOrderDetailPage(@PathVariable Integer orderId, 
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/orders";
        }
        
        List<OrderDetail> orderDetails = orderService.getOrderDetails(orderId);
        
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        
        return "poly/admin/order-detail";
    }
    
    /**
     * Update order status
     */
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(@PathVariable Integer orderId,
                                   @RequestParam Integer newStatus,
                                   RedirectAttributes redirectAttributes) {
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/orders";
        }
        
        // Validate status transition
        String validationMessage = validateStatusTransition(order.getStatusId(), newStatus);
        if (validationMessage != null) {
            redirectAttributes.addFlashAttribute("message", validationMessage);
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/orders/" + orderId;
        }
        
        boolean success = orderService.updateOrderStatus(orderId, newStatus);
        
        if (success) {
            String statusName = getStatusName(newStatus);
            redirectAttributes.addFlashAttribute("message", "✅ Đã cập nhật trạng thái đơn hàng thành: " + statusName);
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Không thể cập nhật trạng thái đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/admin/orders/" + orderId;
    }
    
    /**
     * Validate status transition logic
     */
    private String validateStatusTransition(Integer currentStatus, Integer newStatus) {
        // Không thể chuyển từ trạng thái đã hủy
        if (currentStatus == 5) {
            return "❌ Không thể thay đổi trạng thái của đơn hàng đã hủy!";
        }
        
        // Không thể chuyển từ đã giao về trạng thái trước đó
        if (currentStatus == 4 && newStatus < 4) {
            return "❌ Không thể chuyển đơn hàng đã giao về trạng thái trước đó!";
        }
        
        // Không thể nhảy cóc trạng thái (ví dụ: từ Pending sang Delivered)
        if (newStatus - currentStatus > 1 && newStatus != 5) {
            return "❌ Phải cập nhật trạng thái theo thứ tự: Chờ xử lý → Đang xử lý → Đang giao → Đã giao";
        }
        
        return null; // Valid transition
    }
    
    /**
     * Get status name by ID
     */
    private String getStatusName(Integer statusId) {
        switch (statusId) {
            case 1: return "Chờ xử lý";
            case 2: return "Đang xử lý";
            case 3: return "Đang giao";
            case 4: return "Đã giao";
            case 5: return "Đã hủy";
            default: return "Không xác định";
        }
    }
    
    /**
     * Quick actions for order processing
     */
    @PostMapping("/{orderId}/process")
    public String processOrder(@PathVariable Integer orderId,
                              RedirectAttributes redirectAttributes) {
        return updateOrderStatus(orderId, 2, redirectAttributes); // Processing
    }
    
    @PostMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable Integer orderId,
                           RedirectAttributes redirectAttributes) {
        return updateOrderStatus(orderId, 3, redirectAttributes); // Shipped
    }
    
    @PostMapping("/{orderId}/deliver")
    public String deliverOrder(@PathVariable Integer orderId,
                              RedirectAttributes redirectAttributes) {
        return updateOrderStatus(orderId, 4, redirectAttributes); // Delivered
    }
    
    @PostMapping("/{orderId}/cancel")
    public String cancelOrderAdmin(@PathVariable Integer orderId,
                                  RedirectAttributes redirectAttributes) {
        return updateOrderStatus(orderId, 5, redirectAttributes); // Cancelled
    }
}