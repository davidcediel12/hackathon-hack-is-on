package com.hackathon.blockchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.blockchain.dto.request.UserRegistration;
import com.hackathon.blockchain.service.authentication.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;


    @Test
    void shouldReturnBadRequestWhenBodyIsNotProvided() throws Exception {

        doNothing().when(authService).registerUser(any(), any(), any());


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUsernameIsNotProvided() throws Exception {

        UserRegistration userRegistration = new UserRegistration("foo@f.com", null, "baz");
        validateBadCall(userRegistration);
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsNotProvided() throws Exception {

        UserRegistration userRegistration = new UserRegistration("foo@f.com", "bar", null);
        validateBadCall(userRegistration);
    }


    private void validateBadCall(UserRegistration userRegistration) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistration)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateUser() throws Exception {

        UserRegistration userRegistration = new UserRegistration("foo@f.com", "bar", "baz");

        doNothing().when(authService).registerUser(any(), any(), any());


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistration)))
                .andExpect(status().isOk());
    }

}