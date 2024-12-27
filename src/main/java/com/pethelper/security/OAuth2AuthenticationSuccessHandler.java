package com.pethelper.security;

import com.pethelper.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        String token = tokenService.createToken(authentication);
        response.addHeader("Authorization", "Bearer " + token);
        
        // 프론트엔드 리다이렉트 URL 설정
        getRedirectStrategy().sendRedirect(request, response, "/oauth2/redirect?token=" + token);
    }
} 