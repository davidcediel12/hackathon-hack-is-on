package com.hackathon.blockchain.service.authentication;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.dto.response.SessionCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    void login(UserLogin login, HttpServletRequest request, HttpServletResponse response);

    void registerUser(UserRegistration userRegistration, HttpServletRequest request, HttpServletResponse response);

    SessionCheck checkSession(Authentication authentication);
}
