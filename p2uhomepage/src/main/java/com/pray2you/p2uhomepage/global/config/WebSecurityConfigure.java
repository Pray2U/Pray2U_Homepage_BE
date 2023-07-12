package com.pray2you.p2uhomepage.global.config;

import com.pray2you.p2uhomepage.global.security.handler.OAuth2AuthenticationFailureHandler;
import com.pray2you.p2uhomepage.global.security.handler.OAuth2AuthenticationSuccessHandler;
import com.pray2you.p2uhomepage.global.security.jwt.JwtAccessDeniedHandler;
import com.pray2you.p2uhomepage.global.security.jwt.JwtAuthenticationEntryPoint;
import com.pray2you.p2uhomepage.global.security.jwt.JwtAuthenticationFilter;
import com.pray2you.p2uhomepage.global.security.jwt.JwtTokenProvider;
import com.pray2you.p2uhomepage.global.security.repository.CookieAuthorizationRequestRepository;
import com.pray2you.p2uhomepage.global.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfigure {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .cors()
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .rememberMe().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //요청에 대한 권한 설정
        http.authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated();

        //oauth2Login
        http.oauth2Login()
                .authorizationEndpoint().baseUri("/api/login/oauth2/code")  // 소셜 로그인 url
                .authorizationRequestRepository(cookieAuthorizationRequestRepository)  // 인증 요청을 cookie 에 저장
                .and()
                .redirectionEndpoint().baseUri("/api/login/oauth/redirect/github")  // 소셜 인증 후 redirect url
                .and()
//                userService()는 OAuth2 인증 과정에서 Authentication 생성에 필요한 OAuth2User 를 반환하는 클래스를 지정한다.
                .userInfoEndpoint().userService(customOAuth2UserService)  // 회원 정보 처리
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        http.exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler);

        //jwt filter 설정
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
