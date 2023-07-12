package com.pray2you.p2uhomepage.global.security.jwt;

import com.pray2you.p2uhomepage.global.security.CustomUserDetails;
import com.pray2you.p2uhomepage.global.security.refreshtoken.RefreshToken;
import com.pray2you.p2uhomepage.global.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key SECRET_KEY;
    private final String COOKIE_REFRESH_TOKEN_KEY;
    private final Long ACCESS_TOKEN_EXPIRE_LENGTH = 1000L * 60 * 60; // 1시간
    private final Long REFRESH_TOKEN_EXPIRE_LENGTH = 1000L * 60 * 60 * 24 * 7; // 1주일
    private final String AUTHORITIES_KEY = "role";
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenProvider(@Value("${app.auth.token.secret-key}") String secretKey, @Value("${app.auth.token.refresh-cookie-key}")String cookieKey, RefreshTokenRepository refreshTokenRepository) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.COOKIE_REFRESH_TOKEN_KEY = cookieKey;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAccessToken(Authentication authentication) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_LENGTH);

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String userId = user.getName();
        String githubId = user.getUsername();

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userId)
                .claim(AUTHORITIES_KEY, role)
                .claim("githubId", githubId)
                .setIssuer("p2u")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public void createRefreshToken(Authentication authentication, HttpServletResponse response) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_LENGTH);

        String refreshToken = Jwts.builder()
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .setIssuer("p2u")
                .setIssuedAt(now)
                .setExpiration(validity)
                .compact();

        log.info("refreshToken :" + refreshToken + COOKIE_REFRESH_TOKEN_KEY);

        saveRefreshToken(authentication, refreshToken);
        ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN_KEY, refreshToken)
                .httpOnly(false)
                .secure(true)
                .sameSite("Lax")
                .maxAge(REFRESH_TOKEN_EXPIRE_LENGTH/1000)
                .path("/")
                .build();



        log.info("헤더에 토큰 추가");
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void saveRefreshToken(Authentication authentication, String refreshToken) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        String id = user.getName();

        refreshTokenRepository.save(new RefreshToken(id, refreshToken));
    }

    public Authentication getAuthentication(String accessToken){
        Claims claims = parseClaims(accessToken);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        log.info(claims.getSubject());

        log.info("githubId " + claims.get("githubId", String.class ));
        CustomUserDetails principal = new CustomUserDetails(Long.valueOf(claims.getSubject()), claims.get("githubId", String.class), authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
            return true;
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalStateException e) {
            log.info("JWT 토큰이 잘못되었습니다");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(accessToken).getBody();
        }catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
