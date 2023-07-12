package com.pray2you.p2uhomepage.global.security.service;

import com.pray2you.p2uhomepage.global.security.CustomUserDetails;
import com.pray2you.p2uhomepage.global.security.jwt.JwtTokenProvider;
import com.pray2you.p2uhomepage.global.security.refreshtoken.RefreshToken;
import com.pray2you.p2uhomepage.global.security.repository.RefreshTokenRepository;
import com.pray2you.p2uhomepage.global.security.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.auth.token.refresh-cookie-key}")
    private String cookieKey;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;

    public String refreshToken(HttpServletRequest request, HttpServletResponse response, String oldAccessToken) {
        String oldRefreshToken = CookieUtil.getCookie(request, cookieKey)
                .map(Cookie::getValue).orElseThrow(() -> new RuntimeException("No Refresh Token Cookie"));

        if(!tokenProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("Not Validated Refresh Token");
        }

        Authentication authentication = tokenProvider.getAuthentication(oldAccessToken);
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        RefreshToken savedToken = refreshTokenRepository.findById(String.valueOf(user.getName())).get();

        if (!savedToken.getRefreshToken().equals(oldRefreshToken)) {
            throw new RuntimeException("Not Matched Refresh Token");
        }

        String accessToken = tokenProvider.createAccessToken(authentication);
        tokenProvider.createRefreshToken(authentication, response);

        return accessToken;
    }

}
