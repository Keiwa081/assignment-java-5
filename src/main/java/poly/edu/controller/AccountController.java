package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import poly.edu.model.Account;

@Controller
@RequestMapping("/account")
public class AccountController {
    
    // Trang tài khoản chính
    @GetMapping
    public String accountPage(Model model) {
        // Ví dụ tài khoản giả lập
        Account account = Account.builder()
                .id(1L)
                .username("polyuser")
                .email("user@fpt.edu.vn")
                .fullName("Nguyễn Văn User")
                .activated(true)
                .build();
        
        model.addAttribute("account", account);
        return "poly/account"; // trỏ tới file account.html trong /templates/poly/
    }

    // Trang đăng ký
    @GetMapping("/register")
    public String registerPage() {
        return "poly/register";
    }

    // Trang cập nhật thông tin
    @GetMapping("/update")
    public String updatePage() {
        return "poly/update-account";
    }

    // Trang đổi mật khẩu
    @GetMapping("/doiMatKhau")
    public String doiMatKhauForm() {
        return "poly/doiMatKhau"; 
    }

    // POST - xử lý đổi mật khẩu
    @PostMapping("/doiMatKhau")
    public String doiMatKhau(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "❌ Mật khẩu xác nhận không khớp!");
            return "poly/doiMatKhau";
        }

        // TODO: thêm logic kiểm tra mật khẩu cũ trong DB
        model.addAttribute("message", "✅ Đổi mật khẩu thành công!");
        return "poly/doiMatKhau";
    }

    // Trang quên mật khẩu
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "poly/forgot-password";
    }
}
