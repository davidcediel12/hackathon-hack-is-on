package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.dto.response.SessionCheck;
import com.hackathon.blockchain.service.authentication.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody UserRegistration userRegistration,
                                                    HttpServletRequest request, HttpServletResponse response) {

        authService.registerUser(userRegistration, request, response);

        return ResponseEntity.ok(new GenericResponse("User registered and logged in successfully"));
    }


    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(@Valid @RequestBody UserLogin userLogin,
                                                 HttpServletRequest request, HttpServletResponse response) {

        authService.login(userLogin, request, response);
        return ResponseEntity.ok(new GenericResponse("Login successful"));
    }


    @GetMapping("/check-session")
    public ResponseEntity<SessionCheck> checkSession(Authentication authentication) {


        return ResponseEntity.ok(authService.checkSession(authentication));
    }
}
