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
            handleNaverUser(attributes);
        }
        // 구글 로그인
        else if (attributes.containsKey("sub")) {
            handleGoogleUser(attributes);
        }

        String token = tokenService.createToken(authentication);
        String provider = "";
        if (attributes.get("kakao_account") instanceof Map) {
            provider = "kakao";
        } else if (attributes.get("response") instanceof Map) {
            provider = "naver";
        } else if (attributes.containsKey("sub")) {
            provider = "google";
        }
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token + "&provider=" + provider;
        
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

    private void handleNaverUser(Map<String, Object> attributes) {
        Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
        
        String email = (String) naverResponse.get("email");
        String name = (String) naverResponse.get("name");
        String imageUrl = (String) naverResponse.get("profile_image");
        String providerId = (String) naverResponse.get("id");
        
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

    private void handleGoogleUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String imageUrl = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");
        
        User user = userService.findByEmail(email)
            .map(existingUser -> {
                existingUser.updateOAuth2Info(AuthProvider.GOOGLE, providerId);
                return existingUser;
            })
            .orElseGet(() -> User.builder()
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .build());
        
        User savedUser = userService.saveUser(user);
        log.info("Saved User: {}", savedUser);
    }
} 