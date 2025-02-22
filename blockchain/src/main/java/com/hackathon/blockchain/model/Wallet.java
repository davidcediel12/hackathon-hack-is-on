package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String address;

    private Double balance;
    private Double netWorth;
    private String accountStatus;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "senderWallet")
    private Set<Transaction> sentTransactions;

    @OneToMany(mappedBy = "receiverWallet")
    private Set<Transaction> receivedTransactions;

    @OneToMany(mappedBy = "wallet")
    private Set<Asset> assets;
}
