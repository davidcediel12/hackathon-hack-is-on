package com.hackathon.blockchain.service.authentication.impl;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.response.LoggedUser;
import com.hackathon.blockchain.dto.response.SessionCheck;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.service.authentication.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Override
    public void login(UserLogin login, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login.username(), login.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);

            securityContextRepository.saveContext(securityContext, request, response);
        } catch (InternalAuthenticationServiceException e) {
            log.warn("Invalid credentials", e);
            throw new ApiException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public SessionCheck checkSession(Authentication authentication) {
        return new SessionCheck(new LoggedUser(authentication.getName()));
    }
}
