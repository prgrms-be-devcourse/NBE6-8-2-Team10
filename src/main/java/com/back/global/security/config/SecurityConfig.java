package com.back.global.security.config;

import com.back.global.security.auth.CustomUserDetailsService;
import com.back.global.security.jwt.JwtFilter;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로들
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger 관련 경로들 - 더 구체적으로 설정
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // WebSocket 관련 경로들
                        .requestMatchers("/chat/**").permitAll()     // WebSocket 엔드포인트 허용
                        .requestMatchers("/chat").permitAll()        // WebSocket 핸드셰이크 경로
                        .requestMatchers("/topic/**").permitAll()    // STOMP 구독 경로 허용
                        .requestMatchers("/queue/**").permitAll()    // 개별 사용자 큐 경로 허용
                        .requestMatchers("/user/**").permitAll()     // 사용자별 메시지 경로 허용
                        .requestMatchers("/app/**").permitAll()      // 메시지 전송 경로 허용
                        // 채팅 REST API는 인증 필요로 변경
                        // CORS 설정이 필요한 경우, CORS 필터를 추가해야 합니다.

                        // 정적 리소스
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/*.html").permitAll() // HTML 파일 접근 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        .anyRequest().authenticated()
                )
                // JWT 필터를 조건부로 적용
                .addFilterBefore(new JwtFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)); // H2 콘솔 접근 허용


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
