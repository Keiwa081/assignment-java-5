package poly.edu.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;

@Service("auth")
public class AuthService {
    
    @Autowired
    private AccountDAO accountDAO;
    
    /**
     * Lấy đối tượng xác thực
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Lấy User đăng nhập
     */
    public UserDetails getUser() {
        return (UserDetails) this.getAuthentication().getPrincipal();
    }
    
    /**
     * Lấy tên đăng nhập
     */
    public String getUsername() {
        return this.getAuthentication().getName();
    }
    
    /**
     * Lấy Account entity của user đăng nhập
     */
    public Account getAccount() {
        String username = this.getUsername();
        return accountDAO.findByUsername(username).orElse(null);
    }
    
    /**
     * Lấy AccountId của user đăng nhập
     */
    public Integer getAccountId() {
        Account account = this.getAccount();
        return account != null ? account.getAccountId() : null;
    }
    
    /**
     * Lấy các vai trò của user đăng nhập
     */
    public List<String> getRoles() {
        return this.getAuthentication().getAuthorities().stream()
                .map(authority -> {
                    String auth = authority.getAuthority();
                    // Bỏ prefix "ROLE_" nếu có
                    return auth.startsWith("ROLE_") ? auth.substring(5) : auth;
                })
                .toList();
    }
    
    /**
     * Kiểm tra đăng nhập hay chưa
     */
    public boolean isAuthenticated() {
        String username = this.getUsername();
        return (username != null && !username.equals("anonymousUser"));
    }
    
    /**
     * Kiểm tra vai trò của user đăng nhập
     * 
     * @param rolesToCheck các vai trò cần kiểm tra
     * @return true nếu user đăng nhập có ít nhất một trong các vai trò cần kiểm tra
     */
    public boolean hasAnyRole(String... rolesToCheck) {
        var grantedRoles = this.getRoles();
        return Stream.of(rolesToCheck).anyMatch(role -> grantedRoles.contains(role));
    }
    
    /**
     * Kiểm tra user có tất cả các vai trò
     * 
     * @param rolesToCheck các vai trò cần kiểm tra
     * @return true nếu user có tất cả các vai trò
     */
    public boolean hasAllRoles(String... rolesToCheck) {
        var grantedRoles = this.getRoles();
        return Stream.of(rolesToCheck).allMatch(role -> grantedRoles.contains(role));
    }
    
    /**
     * Kiểm tra user có vai trò cụ thể
     * 
     * @param role vai trò cần kiểm tra
     * @return true nếu user có vai trò
     */
    public boolean hasRole(String role) {
        return this.getRoles().contains(role);
    }
    
    /**
     * Thay đổi đối tượng xác thực với thông tin user mới
     * 
     * @param username tên đăng nhập của user mới
     * @param password mật khẩu đăng nhập của user mới
     * @param roles vai trò của user mới
     */
    public void authenticate(String username, String password, String... roles) {
        UserDetails user = User.withUsername(username)
                .password(password)
                .roles(roles)
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, 
                user.getPassword(),
                user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    /**
     * Đăng xuất
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}