package com.cinemax.usuariosservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewayRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/") || path.startsWith("/internal/")) {
            String gatewayHeader = httpRequest.getHeader("X-Gateway-Request");
            if (gatewayHeader == null || !gatewayHeader.equals("true")) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Direct requests are not allowed.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
