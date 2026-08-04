package com.aish.mvc.config;

import com.aish.mvc.service.auth.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * BỘ LỌC chạy trên MỌI request, đứng trước bộ lọc đăng nhập mặc định của Spring (xem
 * SecurityConfig.addFilterBefore). Nhiệm vụ: đọc JWT trong header Authorization, nếu hợp lệ thì
 * "đăng nhập tạm" cho request đó bằng cách gán Authentication vào SecurityContext — nhờ vậy các
 * @RestController phía sau (và SecurityContextHolder.getContext().getAuthentication()) mới biết
 * ai đang gọi API mà không cần session.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Chạy cho mỗi request. Các bước: (1) đọc header Authorization; (2) không có/không đúng
    // định dạng "Bearer <token>" thì bỏ qua, coi như chưa đăng nhập; (3) token hợp lệ thì tách
    // email + role, dựng Authentication rồi gán vào SecurityContext; (4) luôn cho request đi
    // tiếp (filterChain.doFilter) — việc chặn truy cập do chưa đăng nhập nằm ở SecurityConfig.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {

                String email = jwtUtil.extractUsername(token);

                String role = jwtUtil.extractRole(token);

                // Spring Security yêu cầu tiền tố "ROLE_" để hasRole("ADMIN") khớp đúng.
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_"+role)
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                // Gán "người dùng hiện tại" cho request này — các service sau đó đọc lại qua
                // SecurityContextHolder để biết ai đang thao tác.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}