package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;
import poly.edu.service.MailService;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private AccountDAO accountDAO;

	@Autowired
	private MailService mailService;

	// ================== TRANG TÀI KHOẢN CHÍNH ==================
	@GetMapping
	public String accountPage(HttpSession session, Model model) {
		// ✅ FIX: Đổi từ "user" sang "account"
		Account loggedIn = (Account) session.getAttribute("account");
		if (loggedIn == null) {
			return "redirect:/account/login";
		}

		boolean isAdmin = loggedIn.getRoles().stream()
				.anyMatch(r -> r.getRoleName().equalsIgnoreCase("ADMIN"));

		model.addAttribute("account", loggedIn);
		model.addAttribute("isAdmin", isAdmin);
		return "poly/taikhoan/account";
	}

	// ================== Trang đăng nhập & đăng ký (tab chung) ==================
	@GetMapping("/login")
	public String loginPage(HttpServletRequest request, Model model) {
		String rememberedUser = null;
		for (Cookie c : Optional.ofNullable(request.getCookies()).orElse(new Cookie[0])) {
			if (c.getName().equals("remember-username")) {
				rememberedUser = c.getValue();
				break;
			}
		}
		model.addAttribute("rememberedUser", rememberedUser);
		model.addAttribute("account", new Account());
		return "poly/taikhoan/login-register";
	}

	// ================== Đăng nhập ==================
	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password,
			@RequestParam(required = false) String remember, HttpServletResponse response, HttpSession session,
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

		// ✅ FIX: Đổi từ "user" sang "account"
		session.setAttribute("account", acc);

		if (remember != null) {
			Cookie cookie = new Cookie("remember-username", username);
			cookie.setMaxAge(10 * 24 * 60 * 60);
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
		// ✅ FIX: Đổi từ "user" sang "account"
		Account user = (Account) session.getAttribute("account");
		if (user == null) {
			return "redirect:/account/login";
		}
		model.addAttribute("account", user);
		return "poly/taikhoan/update-account";
	}

	@PostMapping("/update")
	public String updateAccount(@ModelAttribute("account") Account formAccount, HttpSession session, Model model) {
		// ✅ FIX: Đổi từ "user" sang "account"
		Account currentUser = (Account) session.getAttribute("account");
		if (currentUser == null) {
			return "redirect:/account/login";
		}

		try {
			Optional<Account> existingEmail = accountDAO.findByEmail(formAccount.getEmail());
			if (existingEmail.isPresent() && !existingEmail.get().getUsername().equals(currentUser.getUsername())) {
				model.addAttribute("error", "❌ Email này đã được sử dụng bởi tài khoản khác!");
				model.addAttribute("account", currentUser);
				return "poly/taikhoan/update-account";
			}

			currentUser.setEmail(formAccount.getEmail());
			currentUser.setFullName(formAccount.getFullName());
			currentUser.setActive(currentUser.getActive());
			currentUser.setPassword(currentUser.getPassword());
			currentUser.setCreatedAt(currentUser.getCreatedAt());

			accountDAO.save(currentUser);
			// ✅ FIX: Đổi từ "user" sang "account"
			session.setAttribute("account", currentUser);
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
	public String doiMatKhauForm(HttpSession session, Model model) {
		// ✅ Thêm check login
		Account user = (Account) session.getAttribute("account");
		if (user == null) {
			return "redirect:/account/login";
		}
		model.addAttribute("account", user);
		return "poly/taikhoan/doiMatKhau";
	}

	@PostMapping("/doiMatKhau")
	public String doiMatKhau(@RequestParam String oldPassword, @RequestParam String newPassword,
			@RequestParam String confirmPassword, HttpSession session, Model model) {

		// ✅ FIX: Thêm logic đổi mật khẩu thực sự
		Account currentUser = (Account) session.getAttribute("account");
		if (currentUser == null) {
			return "redirect:/account/login";
		}

		if (!currentUser.getPassword().equals(oldPassword)) {
			model.addAttribute("error", "❌ Mật khẩu cũ không đúng!");
			model.addAttribute("account", currentUser);
			return "poly/taikhoan/doiMatKhau";
		}

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("error", "❌ Mật khẩu xác nhận không khớp!");
			model.addAttribute("account", currentUser);
			return "poly/taikhoan/doiMatKhau";
		}

		if (newPassword.length() < 6) {
			model.addAttribute("error", "❌ Mật khẩu mới phải có ít nhất 6 ký tự!");
			model.addAttribute("account", currentUser);
			return "poly/taikhoan/doiMatKhau";
		}

		// ✅ Cập nhật mật khẩu
		currentUser.setPassword(newPassword);
		accountDAO.save(currentUser);
		session.setAttribute("account", currentUser);

		model.addAttribute("message", "✅ Đổi mật khẩu thành công!");
		model.addAttribute("account", currentUser);
		return "poly/taikhoan/doiMatKhau";
	}

	// ================== Quên mật khẩu ==================
	@GetMapping("/forgot")
	public String showForgotPage() {
		return "poly/taikhoan/forgot";
	}

	@PostMapping("/forgot")
	public String processForgot(@RequestParam("email") String input, HttpSession session, Model model) {
		Optional<Account> opt = accountDAO.findByEmail(input);
		if (opt.isEmpty()) {
			opt = accountDAO.findByUsername(input);
		}

		if (opt.isEmpty()) {
			model.addAttribute("error", "❌ Không tìm thấy tài khoản với thông tin này!");
			return "poly/taikhoan/forgot";
		}

		Account acc = opt.get();

		// Tạo mã reset ngẫu nhiên
		String resetCode = java.util.UUID.randomUUID().toString().substring(0, 8);
		acc.setResetCode(resetCode);
		accountDAO.save(acc);

		// Gửi mail
		mailService.sendMail(acc.getEmail(), "Đặt lại mật khẩu - Ứng dụng của bạn",
				"Xin chào " + acc.getFullName() + ",\n\n" + "Mã đặt lại mật khẩu của bạn là: " + resetCode + "\n\n"
						+ "Truy cập trang sau để nhập mã và đặt lại mật khẩu mới:\n"
						+ "http://localhost:8080/account/reset?email=" + acc.getEmail() + "\n\n"
						+ "Trân trọng,\nĐội ngũ hỗ trợ.");

		return "redirect:/account/reset?email=" + acc.getEmail();
	}

	// ================== Xử lý reset mật khẩu ==================
	@GetMapping("/reset")
	public String showResetForm() {
		return "poly/taikhoan/reset";
	}

	@PostMapping("/reset")
	public String processReset(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword,
			@RequestParam String confirmPassword, Model model, HttpSession session) {

		Optional<Account> opt = accountDAO.findByEmail(email);

		if (opt.isEmpty()) {
			model.addAttribute("error", "❌ Email không tồn tại!");
			return "poly/taikhoan/reset";
		}

		Account acc = opt.get();
		if (acc.getResetCode() == null || !acc.getResetCode().equals(code)) {
			model.addAttribute("error", "❌ Mã xác nhận không đúng!");
			return "poly/taikhoan/reset";
		}

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("error", "❌ Mật khẩu xác nhận không khớp!");
			return "poly/taikhoan/reset";
		}

		// Cập nhật mật khẩu và đăng nhập tự động
		acc.setPassword(newPassword);
		acc.setResetCode(null);
		accountDAO.save(acc);

		// ✅ FIX: Đổi từ "user" sang "account"
		session.setAttribute("account", acc);

		return "redirect:/account";
	}
}