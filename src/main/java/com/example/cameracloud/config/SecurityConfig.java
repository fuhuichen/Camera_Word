package com.example.cameracloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 公开访问的端点
                .requestMatchers("/view", "/view/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/login.html", "/admin-dashboard.html", "/test_camera.html").permitAll()
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/login", "/static/**").permitAll()  // 允许访问登录页面和静态资源
                // 管理API需要认证
                .requestMatchers("/api/v1/admin/**").authenticated()
                .requestMatchers("/dashboard", "/admin/**").authenticated()  // 管理页面需要认证
                // 其他请求允许
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")  // 自定义登录页面
                .loginProcessingUrl("/perform_login")  // 登录处理URL
                .defaultSuccessUrl("/dashboard", true)  // 登录成功后的重定向
                .failureUrl("/login?error=true")  // 登录失败后的重定向
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .httpBasic(httpBasic -> {})
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/view", "/view/**", "/api/**", "/login.html", "/perform_login")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails mainAdmin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("MAIN_ADMIN")
            .build();
            
        UserDetails platformAdmin = User.builder()
            .username("platform_admin")
            .password(passwordEncoder().encode("platform123"))
            .roles("PLATFORM_ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(mainAdmin, platformAdmin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
