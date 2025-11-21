package poly.edu;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import poly.edu.service.DaoUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // OPTION 1: Standard encoder (Khuyến nghị - cần migrate password)
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        
        // OPTION 2: Plain text encoder (KHÔNG AN TOÀN - chỉ dùng test)
        // return new CustomPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new DaoUserDetailsManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Bỏ cấu hình mặc định CSRF và CORS
        http.csrf(config -> config.disable())
            .cors(config -> config.disable());

        // Phân quyền sử dụng
        http.authorizeHttpRequests(req -> {
            // Yêu cầu quyền USER để truy cập /home
//            req.requestMatchers("/home").hasRole("USER");
            
            // Các URL yêu cầu quyền ADMIN
            req.requestMatchers("/admin/**").hasRole("ADMIN");
            
            // Các URL yêu cầu quyền EMPLOYEE hoặc ADMIN
            req.requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN");
            
            // Các URL yêu cầu authenticated (đăng nhập)
            req.requestMatchers("/account", "/account/update", "/account/doiMatKhau",
                              "/profile/**", "/orders/**", "/cart/**").authenticated();
            
            // Cho phép truy cập public
            req.requestMatchers("/", "/product/**", "/category/**", 
                              "/search", "/about", "/terms", "/privacy",
                              "/promotions", "/test-db", "/under-construction",
                              "/account/login", "/account/register", 
                              "/account/forgot", "/account/reset",
                              "/css/**", "/js/**", "/images/**", "/assets/**").permitAll();
            
            // Tất cả các request khác cần authenticated
            req.anyRequest().permitAll();
        });

        // Từ chối truy xuất nếu vai trò không phù hợp
        http.exceptionHandling(denied -> 
            denied.accessDeniedPage("/unauthorized.html")
        );

        // Form đăng nhập
        http.formLogin(login -> login
            .loginPage("/account/login")
            .loginProcessingUrl("/account/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .defaultSuccessUrl("/home", true)
            .failureUrl("/account/login?error=true")
            .permitAll()
        );

        // Đăng xuất
        http.logout(logout -> logout
            .logoutUrl("/account/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        );

        return http.build();
    }
}