package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.dao.AccountDAO;
import poly.edu.dao.RoleDAO;
import poly.edu.model.Account;
import poly.edu.model.Role;
import poly.edu.service.AuthService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminEmployeesController {

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    /**
     * Hiển thị danh sách nhân viên
     * URL: /admin/employees
     */
    @GetMapping("/employees")
    public String listEmployees(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String status,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message", "❌ Vui lòng đăng nhập!");
            return "redirect:/account/login";
        }
        
        if (!authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/home";
        }
        
        try {
            // Lấy tất cả tài khoản
            List<Account> allAccounts = accountDAO.findAll();
            
            // Lọc chỉ lấy những tài khoản có role EMPLOYEE
            List<Account> employees = allAccounts.stream()
                .filter(acc -> acc.getRoles().stream()
                    .anyMatch(role -> role.getRoleName().equals("EMPLOYEE")))
                .collect(Collectors.toList());
            
            // Lọc theo tìm kiếm
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                employees = employees.stream()
                    .filter(emp -> 
                        emp.getUsername().toLowerCase().contains(searchLower) ||
                        (emp.getFullName() != null && emp.getFullName().toLowerCase().contains(searchLower)) ||
                        (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
            }
            
            // Lọc theo trạng thái
            if (status != null && !status.isEmpty()) {
                if (status.equals("active")) {
                    employees = employees.stream()
                        .filter(emp -> emp.getActive())
                        .collect(Collectors.toList());
                } else if (status.equals("inactive")) {
                    employees = employees.stream()
                        .filter(emp -> !emp.getActive())
                        .collect(Collectors.toList());
                }
            }
            
            model.addAttribute("employees", employees);
            
            return "poly/employee/employees";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/dashboard";
        }
    }

    /**
     * Hiển thị form thêm nhân viên mới
     * URL: /admin/employees/add
     */
    @GetMapping("/employees/add")
    public String showAddForm(Model model, RedirectAttributes redirectAttributes) {
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        model.addAttribute("employee", new Account());
        return "poly/employee/employees_add";
    }

    /**
     * Xử lý thêm nhân viên mới
     * URL: POST /admin/employees/save
     */
    @PostMapping("/employees/save")
    public String addEmployee(@ModelAttribute("employee") Account employee,
                            @RequestParam String password,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        try {
            // Kiểm tra username đã tồn tại
            if (accountDAO.findByUsername(employee.getUsername()).isPresent()) {
                model.addAttribute("message", "❌ Tên đăng nhập đã tồn tại!");
                model.addAttribute("messageType", "error");
                model.addAttribute("employee", employee);
                return "poly/employee/employees_add";
            }

            // Kiểm tra email đã tồn tại
            if (accountDAO.findByEmail(employee.getEmail()).isPresent()) {
                model.addAttribute("message", "❌ Email đã được sử dụng!");
                model.addAttribute("messageType", "error");
                model.addAttribute("employee", employee);
                return "poly/employee/employees_add";
            }

            // Mã hóa password
            employee.setPassword(passwordEncoder.encode(password));
            employee.setActive(true);
            employee.setCreatedAt(LocalDateTime.now());

            // Gán role EMPLOYEE
            Optional<Role> employeeRole = roleDAO.findByRoleName("EMPLOYEE");
            if (employeeRole.isPresent()) {
                Set<Role> roles = new HashSet<>();
                roles.add(employeeRole.get());
                employee.setRoles(roles);
            } else {
                model.addAttribute("message", "❌ Không tìm thấy role EMPLOYEE trong hệ thống!");
                model.addAttribute("messageType", "error");
                model.addAttribute("employee", employee);
                return "poly/employee/employees_add";
            }

            accountDAO.save(employee);

            redirectAttributes.addFlashAttribute("message", "✅ Thêm nhân viên thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/admin/employees";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "❌ Lỗi khi thêm nhân viên: " + e.getMessage());
            model.addAttribute("messageType", "error");
            model.addAttribute("employee", employee);
            return "poly/employee/employees_add";
        }
    }

    /**
     * Hiển thị form chỉnh sửa nhân viên
     * URL: /admin/employees/edit/{id}
     */
    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        try {
            Optional<Account> employee = accountDAO.findById(id);
            
            if (employee.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy nhân viên!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/employees";
            }

            model.addAttribute("employee", employee.get());
            return "poly/employee/employees_edit";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/employees";
        }
    }

    /**
     * Xử lý cập nhật nhân viên
     * URL: POST /admin/employees/update/{id}
     */
    @PostMapping("/employees/update/{id}")
    public String updateEmployee(@PathVariable("id") int id,
                               @ModelAttribute("employee") Account formEmployee,
                               @RequestParam(required = false) String password,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        try {
            Optional<Account> existingOpt = accountDAO.findById(id);
            
            if (existingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy nhân viên!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/employees";
            }

            Account existing = existingOpt.get();

            // Kiểm tra email trùng
            Optional<Account> emailCheck = accountDAO.findByEmail(formEmployee.getEmail());
            if (emailCheck.isPresent() && emailCheck.get().getAccountId() != id) {
                model.addAttribute("message", "❌ Email đã được sử dụng bởi tài khoản khác!");
                model.addAttribute("messageType", "error");
                model.addAttribute("employee", formEmployee);
                return "poly/employee/employees_edit";
            }

            // Cập nhật thông tin
            existing.setEmail(formEmployee.getEmail());
            existing.setFullName(formEmployee.getFullName());
            existing.setPhone(formEmployee.getPhone());
            existing.setAddress(formEmployee.getAddress());
            existing.setActive(formEmployee.getActive());

            // Nếu có password mới thì mã hóa và cập nhật
            if (password != null && !password.trim().isEmpty()) {
                existing.setPassword(passwordEncoder.encode(password));
            }

            accountDAO.save(existing);

            redirectAttributes.addFlashAttribute("message", "✅ Cập nhật nhân viên thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/admin/employees";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "❌ Lỗi khi cập nhật: " + e.getMessage());
            model.addAttribute("messageType", "error");
            model.addAttribute("employee", formEmployee);
            return "poly/employee/employees_edit";
        }
    }

    /**
     * Vô hiệu hóa nhân viên
     * URL: POST /admin/employees/delete/{id}
     */
    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable("id") int id, 
                                RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        try {
            Optional<Account> employee = accountDAO.findById(id);
            
            if (employee.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy nhân viên!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/employees";
            }

            // Vô hiệu hóa tài khoản thay vì xóa
            Account emp = employee.get();
            emp.setActive(false);
            accountDAO.save(emp);

            redirectAttributes.addFlashAttribute("message", "✅ Vô hiệu hóa nhân viên thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Lỗi khi vô hiệu hóa nhân viên: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/employees";
    }

    /**
     * Kích hoạt lại nhân viên
     * URL: POST /admin/employees/activate/{id}
     */
    @PostMapping("/employees/activate/{id}")
    public String activateEmployee(@PathVariable("id") int id, 
                                  RedirectAttributes redirectAttributes) {
        
        if (!authService.isAuthenticated() || !authService.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("message", "❌ Bạn không có quyền truy cập!");
            return "redirect:/account/login";
        }

        try {
            Optional<Account> employee = accountDAO.findById(id);
            
            if (employee.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "❌ Không tìm thấy nhân viên!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/employees";
            }

            Account emp = employee.get();
            emp.setActive(true);
            accountDAO.save(emp);

            redirectAttributes.addFlashAttribute("message", "✅ Kích hoạt nhân viên thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "❌ Lỗi khi kích hoạt nhân viên: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/employees";
    }
}