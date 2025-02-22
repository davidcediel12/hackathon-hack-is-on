package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
