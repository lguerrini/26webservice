package com.example.webservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class DeviceSecurityConfiguration {
    @Bean
    @Order(1)
    SecurityFilterChain deviceSecurityFilterChain(
            HttpSecurity http,
                        @Value("${app.security.device-api-key:}") String deviceApiKey) throws Exception {
                DeviceApiKeyFilter deviceApiKeyFilter = new DeviceApiKeyFilter(deviceApiKey);
        http.securityMatcher(DeviceApiKeyFilter::matchesDeviceRoute)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(deviceApiKeyFilter,
                        org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("DEVICE"));
        return http.build();
    }
}