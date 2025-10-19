package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountDAO accountDAO;

    // ================== Trang tài khoản chính ==================
    @GetMapping
    public String accountPage(HttpSession session, Model model) {
        Account loggedIn = (Account) session.getAttribute("user");
        if (loggedIn == null) {
            return "redirect:/account/login";
        }
        model.addAttribute("account", loggedIn);
        return "poly/taikhoan/account";
    }

    // ================== Trang đăng nhập & đăng ký (tab chung) ==================
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        // Kiểm tra cookie remember-me
        String rememberedUser = null;
        for (Cookie c : Optional.ofNullable(request.getCookies()).orElse(new Cookie[0])) {
            if (c.getName().equals("remember-username")) {
                rememberedUser = c.getValue();
                break;
            }
        }
        model.addAttribute("rememberedUser", rememberedUser);
        model.addAttribute("account", new Account());
        return "poly/taikhoan/login-register"; // 👉 gộp chung login + register
    }

    // ================== Đăng nhập ==================
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String remember,
            HttpServletResponse response,
            HttpSession session,
            Model model) {

        Optional<Account> opt = accountDAO.findByUsername(username);
        if (opt.isEmpty()) {
            model.addAttribute("error", "❌ Tài khoản không tồn tại!");
            return "poly/taikhoan/login-register";
        }

        Account acc = opt.get();
        if (!acc.getPassword().equals(password)) {
            model.addAttribute("error", "❌ Mật khẩu không đúng!");
            return "poly/taikhoan/login-register";
        }

        if (!acc.getActive()) {
            model.addAttribute("error", "❌ Tài khoản đã bị khóa!");
            return "poly/taikhoan/login-register";
        }

        // Đăng nhập thành công
        session.setAttribute("user", acc);

        // Xử lý ghi nhớ đăng nhập
        if (remember != null) {
            Cookie cookie = new Cookie("remember-username", username);
            cookie.setMaxAge(10 * 24 * 60 * 60); // 10 ngày
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            Cookie cookie = new Cookie("remember-username", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        return "redirect:/account";
    }

    // ================== Đăng ký ==================
    @PostMapping("/register")
    public String register(@ModelAttribute("account") Account account, Model model) {
        if (accountDAO.findByUsername(account.getUsername()).isPresent()) {
            model.addAttribute("error", "❌ Tên đăng nhập đã tồn tại!");
            return "poly/taikhoan/login-register";
        }
        if (accountDAO.findByEmail(account.getEmail()).isPresent()) {
            model.addAttribute("error", "❌ Email đã được sử dụng!");
            return "poly/taikhoan/login-register";
        }

        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        accountDAO.save(account);

        model.addAttribute("message", "✅ Đăng ký thành công! Hãy đăng nhập.");
        return "poly/taikhoan/login-register";
    }

    // ================== Đăng xuất ==================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/account/login";
    }
    
 // ================== Cập nhật thông tin ==================
    @GetMapping("/update")
    public String updatePage(HttpSession session, Model model) {
        // Lấy thông tin người dùng hiện tại từ session
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return "redirect:/account/login";
        }

        model.addAttribute("account", user);
        return "poly/taikhoan/update-account";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("account") Account formAccount,
                                HttpSession session,
                                Model model) {

        // Lấy tài khoản hiện tại từ session
        Account currentUser = (Account) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        try {
            // ====== Kiểm tra email đã tồn tại (trừ chính tài khoản đang đăng nhập) ======
            Optional<Account> existingEmail = accountDAO.findByEmail(formAccount.getEmail());
            if (existingEmail.isPresent() && 
                !existingEmail.get().getUsername().equals(currentUser.getUsername())) {
                
                model.addAttribute("error", "❌ Email này đã được sử dụng bởi tài khoản khác!");
                model.addAttribute("account", currentUser);
                return "poly/taikhoan/update-account";
            }

            // ====== Cập nhật các thông tin hợp lệ ======
            currentUser.setEmail(formAccount.getEmail());
            currentUser.setFullName(formAccount.getFullName());

            // Giữ nguyên các giá trị quan trọng
            currentUser.setActive(currentUser.getActive());
            currentUser.setPassword(currentUser.getPassword());
            currentUser.setCreatedAt(currentUser.getCreatedAt());

            // ====== Lưu vào database ======
            accountDAO.save(currentUser);

            // ====== Cập nhật lại session ======
            session.setAttribute("user", currentUser);

            model.addAttribute("account", currentUser);
            model.addAttribute("message", "✅ Cập nhật thông tin thành công!");

        } catch (Exception e) {
            model.addAttribute("error", "❌ Lỗi khi cập nhật: " + e.getMessage());
            model.addAttribute("account", formAccount);
        }

        return "poly/taikhoan/update-account";
    }


    // ================== Đổi mật khẩu ==================
    @GetMapping("/doiMatKhau")
    public String doiMatKhauForm() {
        return "poly/taikhoan/doiMatKhau"; 
    }

    @PostMapping("/doiMatKhau")
    public String doiMatKhau(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "❌ Mật khẩu xác nhận không khớp!");
            return "poly/taikhoan/doiMatKhau";
        }

        // TODO: kiểm tra oldPassword trong DB
        model.addAttribute("message", "✅ Đổi mật khẩu thành công!");
        return "poly/taikhoan/doiMatKhau";
    }
}
