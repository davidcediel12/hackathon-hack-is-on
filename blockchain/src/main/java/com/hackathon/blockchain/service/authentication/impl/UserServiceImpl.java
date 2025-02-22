package com.hackathon.blockchain.service.authentication.impl;

import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.service.authentication.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("❌ Invalid credentials", HttpStatus.UNAUTHORIZED));
    }


    @Override
    @Transactional
    public void registerUser(UserRegistration userRegistration) {

        String encodedPassword = passwordEncoder.encode(userRegistration.password());

        User user = User.builder()
                .email(userRegistration.email())
                .username(userRegistration.username())
                .password(encodedPassword)
                .build();

        userRepository.save(user);
    }
}
