package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.dto.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtResponse> signIn(@RequestPart("username") String username,
                                              @RequestPart("password") String password) {
        JwtResponse jwtResponse = authService.signIn(username, password);
        return ResponseEntity.ok(jwtResponse);
    }

}
