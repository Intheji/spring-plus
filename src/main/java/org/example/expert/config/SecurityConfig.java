package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final SecurityJwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 암호화
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // jwt 기반 인증을 사용하니까 csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                //  세션 폼 로그인 기본 로그인 방식 사용 안 하니까 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // UsernamePasswordAuthenticationFilter, DefaultLoginPageGeneratingFilter 비활성화
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)

                // jwt 기반 인증은 서버에 세션을 저장하지 않으니까 stateless로 설정
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // UsernamePasswordAuthenticationFilter 전에 jwt 필터를 실행하게 등록하고
                // -> 요청이 들어오면 먼저 jwt 검증 뒤에 인증 정보를 SecurityContext에 저장
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 인가 정책 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인이랑 회원가입 관련은 토큰 없이 접근 가능
                        .requestMatchers(request ->
                                request.getRequestURI().startsWith("/auth")).permitAll()

                        // 챌린지반 실습하면서 썼던 Prometheus, Actuator 모니터링 경로는 외부 수집을 해야 되니까 열어
                        .requestMatchers("/actuator/**").permitAll()

                        // 관리자 경로는 ADMIN 권한이 있는 사용자만 접근
                        .requestMatchers(request ->
                                request.getRequestURI().startsWith("/admin")).hasAuthority(UserRole.ADMIN.name())

                        // 그 외에는 인증 필요~
                        .anyRequest().authenticated()
                )
                .build();
    }
}
