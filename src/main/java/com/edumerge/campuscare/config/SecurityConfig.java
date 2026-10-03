package com.edumerge.campuscare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // allow AJAX/REST calls if needed
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login", "/register", "/auth/**", "/error").permitAll()
                .requestMatchers("/admin/**", "/settings/**", "/staff-mgmt/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/staff/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/tickets/**", "/api/**", "/profile/**", "/notifications/**", "/reports/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    String role = authority.getAuthority();
                    if ("ROLE_ADMIN".equals(role)) {
                        response.sendRedirect("/admin/dashboard");
                        return;
                    } else if ("ROLE_STAFF".equals(role)) {
                        response.sendRedirect("/staff/dashboard");
                        return;
                    } else if ("ROLE_STUDENT".equals(role)) {
                        response.sendRedirect("/student/dashboard");
                        return;
                    }
                }
                response.sendRedirect("/dashboard");
            }
        };
    }
}
