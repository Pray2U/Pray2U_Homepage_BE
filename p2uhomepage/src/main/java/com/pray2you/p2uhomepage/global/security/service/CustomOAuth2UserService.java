package com.pray2you.p2uhomepage.global.security.service;

import com.pray2you.p2uhomepage.domain.model.Role;
import com.pray2you.p2uhomepage.domain.user.entity.User;
import com.pray2you.p2uhomepage.domain.user.repository.UserRepository;
import com.pray2you.p2uhomepage.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);

        User user = saveOrUpdate(oAuth2User);
        log.info(user.getGithubId());

        return CustomUserDetails.create(user, oAuth2User.getAttributes());
    }

    private User saveOrUpdate(OAuth2User oAuth2User){
        User oAuthUser = User.builder()
                .githubId(oAuth2User.getAttribute("login"))
                .username(oAuth2User.getAttribute("name"))
                .profileImgUrl(oAuth2User.getAttribute("avatar_url"))
                .phoneNumber("010-0000-0000")
                .email("test@test.com")
                .role(Role.ROLE_GUEST)
                .build();

        User user = userRepository.findByGithubId(oAuthUser.getGithubId())
                .map(entity -> entity.update(oAuthUser))
                .orElse(oAuthUser);

        return userRepository.save(user);
    }

}
