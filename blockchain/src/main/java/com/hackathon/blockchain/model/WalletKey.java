package com.hackathon.blockchain.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class WalletKey {

    @Id
    private Long id;


    @OneToOne
    private Wallet wallet;

    private String publicKey;
    private String privateKey;
}
