package com.hackathon.blockchain.service.authentication.impl;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.dto.response.LoggedUser;
import com.hackathon.blockchain.dto.response.SessionCheck;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.service.authentication.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hackathon.blockchain.utils.MessageConstants.INVALID_CREDENTIALS;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    @Override
    public void login(UserLogin login, HttpServletRequest request, HttpServletResponse response) {

        authenticateUser(login.username(), login.password(), request, response);

    }


    @Transactional
    @Override
    public void registerUser(UserRegistration userRegistration,
                             HttpServletRequest request, HttpServletResponse response) {


        if (userRepository.existsByUsername(userRegistration.username())) {
            throw new ApiException(null, HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(userRegistration.password());


        User user = User.builder()
                .email(userRegistration.email())
                .username(userRegistration.username())
                .password(encodedPassword)
                .build();

        userRepository.save(user);

        authenticateUser(user.getUsername(), userRegistration.password(), request, response);
    }


    private void authenticateUser(String username, String password,
                                  HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);
            securityContextRepository.saveContext(context, request, response);

        } catch (AuthenticationException e) {
            log.warn("Invalid credentials", e);
            throw new ApiException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public SessionCheck checkSession(Authentication authentication) {
        return new SessionCheck(new LoggedUser(authentication.getName()));
    }
}
