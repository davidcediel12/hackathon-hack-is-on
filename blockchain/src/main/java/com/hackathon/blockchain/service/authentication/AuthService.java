package com.hackathon.blockchain.service.authentication;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.dto.response.SessionCheck;
import org.springframework.security.core.Authentication;

public interface AuthService {

    void login(UserLogin login);

    void registerUser(UserRegistration userRegistration);

    SessionCheck checkSession(Authentication authentication);
}
