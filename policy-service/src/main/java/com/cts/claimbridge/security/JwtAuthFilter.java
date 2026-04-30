package com.cts.claimbridge.security;
import com.cts.claimbridge.service.JwtService;
import com.cts.claimbridge.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthService authService;

    public JwtAuthFilter(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
     throws IOException , ServletException {
          String authHeader = request.getHeader("Authorization");
          if(authHeader != null && authHeader.startsWith("Bearer "))
          {
               String token = authHeader.substring(7);
               String userName = jwtService.extractUserName(token);
               String role = jwtService.extractRole(token);

              if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null)
              {
                   UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                          userName,
                          "",
                           Collections.singleton(new SimpleGrantedAuthority(role))
                  );
                   UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails , null , userDetails.getAuthorities());
                   SecurityContextHolder.getContext().setAuthentication(authToken);
              }
          }
          filterChain.doFilter(request , response);
    }
}