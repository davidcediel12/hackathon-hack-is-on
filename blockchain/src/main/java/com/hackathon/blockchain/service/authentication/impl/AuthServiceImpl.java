package com.hackathon.blockchain.service.authentication.impl;

import com.hackathon.blockchain.dto.request.UserLogin;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.dto.response.LoggedUser;
import com.hackathon.blockchain.dto.response.SessionCheck;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.service.authentication.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Override
    public void login(UserLogin login) {

        authenticateUser(login.username(), login.password());

    }


    @Transactional
    @Override
    public void registerUser(UserRegistration userRegistration) {


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

        authenticateUser(user.getUsername(), userRegistration.password());
    }


    private void authenticateUser(String username, String password) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
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
