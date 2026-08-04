package com.aish.mvc.config;

import com.aish.mvc.exception.RestAccessDeniedHandler;
import com.aish.mvc.exception.RestAuthenticationEntryPoint;
import com.aish.mvc.security.CustomOAuth2UserService;
import com.aish.mvc.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * CẤU HÌNH BẢO MẬT trung tâm — khai báo đường dẫn nào công khai, đường dẫn nào cần vai trò gì,
 * gắn JwtAuthFilter vào chuỗi lọc, và cấu hình đăng nhập OAuth2 (Google/GitHub).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    // Khai báo toàn bộ luật phân quyền cho request HTTP. Đọc theo thứ tự từ trên xuống, luật
    // khớp trước được áp dụng trước.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API thuần JSON/JWT, không dùng cookie -> tắt CSRF
                .cors(Customizer.withDefaults())
//                .sessionManagement(s ->
//                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint auth trước khi đăng nhập được -> ai cũng gọi được.
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/verify-otp",
                                "/api/auth/resend-otp",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-forgot-password",
                                "/api/auth/reset-password",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/uploads/**"
                        )
                        .permitAll()

                        // Toàn bộ khu vực quản trị chỉ dành cho vai trò ADMIN.
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // DEC-030: Subject là admin-managed — chỉ GET (duyệt danh sách) mở cho
                        // mọi user đã đăng nhập, các thao tác ghi phải là ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/subjects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")

                        .requestMatchers("/api/user/**")
                        .hasAnyRole("USER","ADMIN")
                        // Mọi request còn lại đều bắt buộc đã đăng nhập (JWT hợp lệ).
                        .anyRequest().authenticated()
                )
                // Chưa đăng nhập / không đủ quyền thì trả lỗi JSON thay vì trang login mặc định của Spring.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                // Cấu hình đăng nhập OAuth2: dùng CustomOAuth2UserService để nạp user (vá email
                // thiếu của GitHub), thành công thì chạy OAuth2SuccessHandler (tạo/tìm user + phát JWT).
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {

                            exception.printStackTrace();

                            response.sendRedirect(
                                    "http://localhost:5173/login?error=oauth_failed"
                            );
                        })
                )
                // Chạy JwtAuthFilter TRƯỚC bộ lọc đăng nhập mặc định, để mọi request đã gắn JWT
                // đều được nhận diện danh tính trước khi vào các luật authorizeHttpRequests ở trên.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}
