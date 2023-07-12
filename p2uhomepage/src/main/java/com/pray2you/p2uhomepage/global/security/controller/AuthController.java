package com.pray2you.p2uhomepage.global.security.controller;

import com.pray2you.p2uhomepage.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> accessToken){

        String token = authService.refreshToken(request, response, accessToken.get("accessToken"));
        Map<String , Object> result = new HashMap<>();
        result.put("msg","토큰 갱신에 성공했습니다.");
        result.put("data", token);

        return ResponseEntity.ok().body(result);
    }
}
