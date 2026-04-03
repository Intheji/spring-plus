package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 오또리제이션 헤더에서 jwt 토큰 값을 가져옴
        String bearerJwt = request.getHeader("Authorization");

        // 오또리제이션 헤더가 없거나 Bearer 형식이 아니면
        if (bearerJwt == null || !bearerJwt.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bearer 접두사 제거
            String jwt = jwtUtil.substringToken(bearerJwt);

            // jwt를 검증하고 claims(사용자 정보) 추출
            Claims claims = jwtUtil.extractClaims(jwt);
            setAuthentication(claims);
            filterChain.doFilter(request, response);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    private void setAuthentication(Claims claims) {
        AuthUser authUser = createAuthUser(claims);

        // Spring Security가 인식할 수 있게 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of(new SimpleGrantedAuthority(authUser.getUserRole().name()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private AuthUser createAuthUser (Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());

        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        String role = claims.get("userRole", String.class);

        // jwt에서 꺼낸 정보 객체 변환
        return new AuthUser(
                userId,
                email,
                nickname,
                UserRole.of(role)
        );

    }
}
