package com.paypilot.backend.security;

import com.paypilot.backend.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtService.parseToken(token);

            Long userId = Long.valueOf(claims.getSubject());
            Long merchantId = claims.get("merchantId", Long.class);
            UserRole role = UserRole.valueOf(
                    claims.get("role", String.class)
            );

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(userId, merchantId, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role.name()
                                    )
                            )
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}