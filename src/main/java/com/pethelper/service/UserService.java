package com.pethelper.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.pethelper.entity.User;
import com.pethelper.entity.User.AuthProvider;
import com.pethelper.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdateUser(OAuth2User oAuth2User) {
        // OAuth2User에서 필요한 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 제공자별로 다른 처리 (예: 카카오)
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String email = (String) kakaoAccount.get("email");
            String imageUrl = (String) kakaoAccount.get("profile_image_url");

            // 기존 사용자 찾기
            User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    // updateProfile() 대신 updateOAuth2Info 사용
                    existingUser.updateOAuth2Info(AuthProvider.KAKAO, String.valueOf(attributes.get("id")));
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 새 사용자 생성
                    return User.builder()
                        .email(email)
                        .provider(AuthProvider.KAKAO)
                        .providerId(String.valueOf(attributes.get("id")))
                        .imageUrl(imageUrl)
                        .build();
                });
            
            return userRepository.save(user);
        }
        
        // 다른 제공자들에 대한 처리...
        return null;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
} 