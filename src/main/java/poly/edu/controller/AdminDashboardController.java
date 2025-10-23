package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;
import poly.edu.repository.ProductRepository;
import poly.edu.service.OrderService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AccountDAO accountDAO;
    
    /**
     * Check if current user is admin
     */
    private boolean isAdmin(HttpSession session) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) return false;
        
        return account.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getRoleName()));
    }
    
    /**
     * Admin Dashboard Home
     */
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        // Check if user is admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập trang này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        // Get statistics
        try {
            // Total orders
            long totalOrders = orderService.getAllOrders().size();
            
            // Total products
            long totalProducts = productRepository.count();
            
            // Total users
            long totalUsers = accountDAO.count();
            
            // Total revenue (from delivered orders only)
            Double totalRevenue = orderService.getTotalRevenue();
            if (totalRevenue == null) {
                totalRevenue = 0.0;
            }
            
            // Add to model
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalRevenue", totalRevenue);
            
            // Order statistics by status
            long pendingOrders = orderService.countOrdersByStatus(1);
            long processingOrders = orderService.countOrdersByStatus(2);
            long shippedOrders = orderService.countOrdersByStatus(3);
            long deliveredOrders = orderService.countOrdersByStatus(4);
            long cancelledOrders = orderService.countOrdersByStatus(5);
            
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("processingOrders", processingOrders);
            model.addAttribute("shippedOrders", shippedOrders);
            model.addAttribute("deliveredOrders", deliveredOrders);
            model.addAttribute("cancelledOrders", cancelledOrders);
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalProducts", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", 0.0);
        }
        
        return "poly/admin/dashboard";
    }
    
    /**
     * Redirect /admin to /admin/dashboard
     */
    @GetMapping
    public String adminRedirect() {
        return "redirect:/admin/dashboard";
    }
}