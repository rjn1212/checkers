package com.gitgames.checkers.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class PlatformAuthFilter implements Filter {

    @Value("${platform.jwt.secret:}")
    private String jwtSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && !jwtSecret.isEmpty()) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret))
                        .build()
                        .verify(token);
                httpRequest.setAttribute("platformUserId", jwt.getSubject());
                httpRequest.setAttribute("platformUsername", jwt.getClaim("username").asString());
            } catch (JWTVerificationException e) {
                // Continue anonymously — token invalid but don't block
            }
        }

        chain.doFilter(request, response);
    }
}
