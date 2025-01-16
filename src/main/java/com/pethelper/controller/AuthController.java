package com.pethelper.controller;

import com.pethelper.dto.LoginRequest;
import com.pethelper.dto.SignUpRequest;
import com.pethelper.service.AuthService;
import com.pethelper.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationSuccessHandler successHandler;

    private final TokenBlacklistService tokenBlacklistService;

    @Value("${oauth2.logout.google.url}")
    private String googleLogoutUrl;
    
    @Value("${oauth2.logout.kakao.url}")
    private String kakaoLogoutUrl;
    
    @Value("${oauth2.logout.naver.url}")
    private String naverLogoutUrl;
    
    @Value("${oauth2.logout.redirect-uri}")
    private String logoutRedirectUri;
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/login/oauth2/code/kakao")
    public void handleKakaoRedirect(HttpServletRequest request, HttpServletResponse response, 
                                  @RequestParam("code") String code, 
                                  Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @GetMapping("/login/oauth2/code/google")
    public void handleGoogleRedirect(HttpServletRequest request, HttpServletResponse response, 
                                      @RequestParam("code") String code, 
                                      @RequestParam("state") String state, 
                                      Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @GetMapping("/login/oauth2/code/naver")
    public void handleNaverRedirect(HttpServletRequest request, HttpServletResponse response, 
                                      @RequestParam("code") String code, 
                                      @RequestParam("state") String state, 
                                      Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @SuppressWarnings("null")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            tokenBlacklistService.addToBlacklist(token);
        }

        new SecurityContextLogoutHandler().logout(request, response, null);
        
        String provider = request.getParameter("provider");
        
        if ("kakao".equals(provider)) {
            try {
                // 카카오 로그아웃 API 호출 (토큰 만료)
                String kakaoLogout = "https://kapi.kakao.com/v1/user/logout";
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                // 먼저 토큰을 만료시킴
                restTemplate.exchange(
                    kakaoLogout,
                    HttpMethod.POST,
                    entity,
                    String.class
                );

                // 그 다음 로그아웃 페이지로 리다이렉트
                String kakaoLogoutRedirect = kakaoLogoutUrl + 
                    "?client_id=" + kakaoClientId + 
                    "&logout_redirect_uri=" + logoutRedirectUri;
                
                return ResponseEntity.ok().body(Map.of("logoutUrl", kakaoLogoutRedirect));
            } catch (HttpClientErrorException e) {
                return ResponseEntity.ok().body(Map.of("logoutUrl", logoutRedirectUri));
            }
        } else if ("naver".equals(provider)) {
            try {
                // 네이버 토큰 삭제 API 호출
                String naverTokenDeleteUrl = naverLogoutUrl +
                    "?grant_type=delete" +
                    "&client_id=" + naverClientId +
                    "&client_secret=" + naverClientSecret +
                    "&access_token=" + token +
                    "&service_provider=NAVER";

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                restTemplate.exchange(
                    naverTokenDeleteUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
                );
            } catch (HttpClientErrorException e) {
                // 에러가 발생해도 계속 진행
            }
            return ResponseEntity.ok().body(Map.of("logoutUrl", logoutRedirectUri));
        }

        String logoutUrl = getOAuthLogoutUrl(provider);
        return ResponseEntity.ok().body(Map.of("logoutUrl", logoutUrl));
    }

    private String getOAuthLogoutUrl(String provider) {
        if (provider == null) return null;
        
        switch (provider) {
            case "google":
                return googleLogoutUrl;
            case "kakao":
            case "naver":
                return logoutRedirectUri;
            default:
                return null;
        }
    }
} 