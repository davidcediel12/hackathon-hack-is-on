package com.hackathon.blockchain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.blockchain.dto.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final ObjectMapper objectMapper;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(requests ->
                        requests.requestMatchers("/auth/register", "/auth/login", "/health",
                                        "/market/**", "/blockchain/**").permitAll()
                                .requestMatchers("/auth/logout").authenticated()
                                .anyRequest().authenticated())
                .logout(logout ->
                        logout
                                .permitAll(false)
                                .logoutUrl("/auth/logout")
                                .deleteCookies("JSESSIONID")
                                .logoutSuccessHandler(customLogoutSuccessHandler())
                                .clearAuthentication(true)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {

        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            GenericResponse genericResponse = new GenericResponse("Logged out successfully");

            response.getWriter().write(objectMapper.writeValueAsString(genericResponse));
            response.getWriter().flush();
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
