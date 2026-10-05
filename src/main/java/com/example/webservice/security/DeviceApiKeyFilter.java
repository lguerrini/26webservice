package com.example.webservice.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class DeviceApiKeyFilter extends OncePerRequestFilter {
    public static final String API_KEY_HEADER = "X-Device-Key";

    private final String expectedApiKey;

    public DeviceApiKeyFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public static boolean matchesDeviceRoute(HttpServletRequest request) {
        boolean getDevice = "GET".equals(request.getMethod())
                && "/api/iot".equals(request.getServletPath());
        boolean postData = "POST".equals(request.getMethod())
                && "/api/data".equals(request.getServletPath());
        return (getDevice || postData) && request.getHeader(API_KEY_HEADER) != null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !matchesDeviceRoute(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String suppliedApiKey = request.getHeader(API_KEY_HEADER);
        if (!hasValidConfiguration() || suppliedApiKey == null || !matchesKey(suppliedApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        DeviceIdentity device = new DeviceIdentity("arduino");
        var authentication = new UsernamePasswordAuthenticationToken(
                device,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DEVICE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private boolean hasValidConfiguration() {
        return expectedApiKey != null && !expectedApiKey.isBlank();
    }

    private boolean matchesKey(String suppliedApiKey) {
        return MessageDigest.isEqual(
                expectedApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedApiKey.getBytes(StandardCharsets.UTF_8));
    }
}