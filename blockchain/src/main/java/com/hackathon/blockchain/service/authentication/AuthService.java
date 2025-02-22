package com.hackathon.blockchain.service.authentication;

import com.hackathon.blockchain.dto.request.UserLogin;

public interface AuthService {

    void login(UserLogin login);
}
