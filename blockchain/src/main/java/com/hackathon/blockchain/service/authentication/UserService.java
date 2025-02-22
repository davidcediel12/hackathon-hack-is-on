package com.hackathon.blockchain.service.authentication;

import com.hackathon.blockchain.dto.request.UserRegistration;

public interface UserService {

    void registerUser(UserRegistration userRegistration);
}
