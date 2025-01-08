package com.pethelper.security;

import com.pethelper.service.TokenService;
import com.pethelper.service.UserService;
import com.pethelper.entity.User;
import com.pethelper.entity.User.AuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserService userService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.info("OAuth2 User Attributes: {}", attributes);
        
        // 카카오 로그인
        if (attributes.get("kakao_account") instanceof Map) {
            handleKakaoUser(attributes);
        }
        // 네이버 로그인
        else if (attributes.get("response") instanceof Map) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            
            // 사용자 정보
            String email = (String) response.get("email");
            String name = (String) response.get("name");
            String imageUrl = (String) response.get("profile_image");
            String providerId = (String) response.get("id");
            
            // User 객체 생성 또는 업데이트
            User user = userService.findByEmail(email)
                .map(existingUser -> {
                    existingUser.updateOAuth2Info(AuthProvider.NAVER, providerId);
                    return existingUser;
                })
                .orElseGet(() -> User.builder()
                    .email(email)
                    .name(name)
                    .imageUrl(imageUrl)
                    .provider(AuthProvider.NAVER)
                    .providerId(providerId)
                    .build());
            
            User savedUser = userService.saveUser(user);
            log.info("Saved User: {}", savedUser);
        }

        String token = tokenService.createToken(authentication);
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token;
        
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void handleKakaoUser(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");
        String imageUrl = (String) profile.get("profile_image_url");
        String providerId = String.valueOf(attributes.get("id"));
        
        User user = userService.findByEmail(email)
            .map(existingUser -> {
                existingUser.updateOAuth2Info(AuthProvider.KAKAO, providerId);
                return existingUser;
            })
            .orElseGet(() -> User.builder()
                .email(email)
                .name(nickname)
                .imageUrl(imageUrl)
                .provider(AuthProvider.KAKAO)
                .providerId(providerId)
                .build());
        
        User savedUser = userService.saveUser(user);
        log.info("Saved User: {}", savedUser);
    }
} 