package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        return "poly/account"; 
    }

    // ================== Đăng ký ==================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("account", new Account());
        return "poly/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("account") Account account, Model model) {
        // TODO: lưu account vào DB
        model.addAttribute("message", "✅ Đăng ký thành công!");
        return "poly/register";
    }

    // ================== Cập nhật ==================
    @GetMapping("/update")
    public String updatePage(Model model) {
        // Ví dụ giả lập dữ liệu có sẵn
        Account account = Account.builder()
                .id(1L)
                .username("polyuser")
                .email("user@fpt.edu.vn")
                .fullName("Nguyễn Văn User")
                .activated(true)
                .build();
        model.addAttribute("account", account);
        return "poly/update-account";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("account") Account account, Model model) {
        // TODO: lưu thay đổi vào DB
        model.addAttribute("message", "✅ Cập nhật thông tin thành công!");
        return "poly/update-account";
    }

    // ================== Đổi mật khẩu ==================
    @GetMapping("/doiMatKhau")
    public String doiMatKhauForm() {
        return "poly/doiMatKhau"; 
    }

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

        // TODO: kiểm tra oldPassword trong DB
        model.addAttribute("message", "✅ Đổi mật khẩu thành công!");
        return "poly/doiMatKhau";
    }
}
