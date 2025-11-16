package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.dao.AccountDAO;
import poly.edu.repository.ProductRepository;
import poly.edu.service.AuthService;
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
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/account/login";
        }
        
        if (!authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập trang này!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/home";
        }
        
        try {
            long totalOrders = orderService.getAllOrders().size();
            long totalProducts = productRepository.count();
            long totalUsers = accountDAO.count();
            
            Double totalRevenue = orderService.getTotalRevenue();
            if (totalRevenue == null) {
                totalRevenue = 0.0;
            }
            
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalRevenue", totalRevenue);
            
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
    
    @GetMapping
    public String adminRedirect() {
        return "redirect:/admin/dashboard";
    }
}