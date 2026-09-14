package com.web.pfc.SpringPfc.config;

import com.web.pfc.SpringPfc.Repository.UsuarioRep;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRep usuarioRepo;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRep usuarioRepo) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.tokenValido(token)) {
                String email = jwtUtil.extrairEmail(token);
                usuarioRepo.findByEmail(email).ifPresent(usuario -> {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}