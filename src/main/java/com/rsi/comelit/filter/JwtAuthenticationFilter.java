package com.rsi.comelit.filter;

import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.UserRepository;
import com.rsi.comelit.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static String email;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/login") || path.equals("/api/auth/register") || path.equals("/api/v1/auth/forgot-password") || path.equals("/api/v1/auth/verify-email") || path.equals("/api/v1/auth/reset-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        email = jwtService.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

            if (utilisateur != null && jwtService.validateToken(token, utilisateur)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(utilisateur, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
