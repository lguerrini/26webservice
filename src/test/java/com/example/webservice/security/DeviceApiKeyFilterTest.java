package com.example.webservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class DeviceApiKeyFilterTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validSharedKeyAuthenticatesArduinoDevice() throws Exception {
        DeviceApiKeyFilter filter = new DeviceApiKeyFilter("test-device-key");
        MockHttpServletRequest request = deviceRequest("test-device-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(authentication.getPrincipal()).isEqualTo(new DeviceIdentity("arduino"));
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_DEVICE");
    }

    @Test
    void invalidKeyIsRejected() throws Exception {
        DeviceApiKeyFilter filter = new DeviceApiKeyFilter("test-device-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(deviceRequest("wrong-key"), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockHttpServletRequest deviceRequest(String apiKey) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/data");
        request.setServletPath("/api/data");
        request.addHeader(DeviceApiKeyFilter.API_KEY_HEADER, apiKey);
        return request;
    }
}