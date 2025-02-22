package com.hackathon.blockchain.service.authentication.impl;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.service.UserService;
import com.hackathon.blockchain.service.authentication.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void login(UserLogin login) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.username(), login.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (InternalAuthenticationServiceException e) {
            log.warn("Invalid credentials", e);
            throw new ApiException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
