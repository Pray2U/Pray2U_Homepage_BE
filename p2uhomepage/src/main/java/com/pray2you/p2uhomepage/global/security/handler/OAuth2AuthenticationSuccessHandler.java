package com.pray2you.p2uhomepage.global.security.handler;

import com.pray2you.p2uhomepage.global.exception.ErrorCode.ErrorCode;
import com.pray2you.p2uhomepage.global.exception.ErrorCode.UserErrorCode;
import com.pray2you.p2uhomepage.global.exception.RestApiException;
import com.pray2you.p2uhomepage.global.security.jwt.JwtTokenProvider;
import com.pray2you.p2uhomepage.global.security.repository.CookieAuthorizationRequestRepository;
import com.pray2you.p2uhomepage.global.security.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.pray2you.p2uhomepage.global.security.repository.CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;
    private final JwtTokenProvider tokenProvider;
    private final CookieAuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed");
            return;
        }
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            ErrorCode errorCode = UserErrorCode.NOT_MATCHED_REDIRECT_URI;
            setResponse(response, errorCode);
        }
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        String accessToken = tokenProvider.createAccessToken(authentication);

        tokenProvider.createRefreshToken(authentication, response);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri){
        URI clientRedirectUri = URI.create(uri);
        URI authorizedUri = URI.create(redirectUri);

        return authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost()) && authorizedUri.getPort() == clientRedirectUri.getPort();
    }

    private void setResponse(HttpServletResponse response, ErrorCode errorCode) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        StringBuilder responseJson = new StringBuilder();
        responseJson.append("{")
                .append("\"status\": \"").append(errorCode.getHttpStatus()).append("\", ")
                .append("\"error\": \"").append(errorCode.name()).append("\", ")
                .append("\"msg\": \"").append(errorCode.getMessage()).append("\"")
                .append("}");
    }
}
