package com.zhunong.mall.config;

import com.zhunong.mall.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/home/**").permitAll()
                .requestMatchers("/category/**").permitAll()
                .requestMatchers("/product/**").permitAll()
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/common/**").permitAll()
                .requestMatchers("/cart/**").permitAll()
                .requestMatchers("/order/**").permitAll()
                .requestMatchers("/address/**").permitAll()
                .requestMatchers("/favorite/**").permitAll()
                .requestMatchers("/activity/**").permitAll()
                .requestMatchers("/review/**").permitAll()
                .requestMatchers("/config/**").permitAll()
                .requestMatchers("/banner/**").permitAll()
                .requestMatchers("/doc.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/farmer/**").permitAll()
                .requestMatchers("/shops/**").permitAll()
                .requestMatchers("/messages/**").permitAll()
                .requestMatchers("/reviews/**").permitAll()
                .requestMatchers("/reports/**").permitAll()
                .requestMatchers("/statistics/**").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    public static class JwtAuthenticationFilter extends OncePerRequestFilter {
        
        private final JwtUtil jwtUtil;
        
        public JwtAuthenticationFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }
        
        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    if (username != null && jwtUtil.verifyToken(token) != null) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (Exception e) {
                    // Token invalid, continue as anonymous
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
