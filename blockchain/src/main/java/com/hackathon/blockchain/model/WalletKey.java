package com.hackathon.blockchain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class WalletKey {

    @Id
    @GeneratedValue
    private Long id;


    @OneToOne
    private Wallet wallet;

    @Lob
    private String publicKey;
    @Lob
    private String privateKey;
}
