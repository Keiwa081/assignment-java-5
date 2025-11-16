package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import poly.edu.dao.AccountDAO;
import poly.edu.model.Account;

import java.util.Collection;
import java.util.stream.Collectors;

public class DaoUserDetailsManager implements UserDetailsService {
    
    @Autowired
    private AccountDAO accountDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user theo username hoặc email
        Account account = accountDAO.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username or email: " + username));
        
        // Kiểm tra tài khoản có active không
        if (!account.getActive()) {
            throw new UsernameNotFoundException("Account is not active: " + username);
        }
        
        // Lấy password
        String password = account.getPassword();
        
        // Chuyển đổi roles thành authorities
        Collection<? extends GrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
        
        // Trả về UserDetails
        return User.builder()
                .username(account.getUsername())
                .password(password)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!account.getActive())
                .credentialsExpired(false)
                .disabled(!account.getActive())
                .build();
    }
}