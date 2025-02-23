package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long blockIndex;


    private String previousHash;

    @Timestamp
    LocalDateTime timestamp;

    private Long nonce;

    private String hash;

    private Boolean isGenesis;

    @OneToMany(mappedBy = "block")
    Set<Transaction> transactions;


    public String calculateHash() {
        String properties = blockIndex + previousHash + nonce + timestamp;
        return DigestUtils.sha256Hex(properties);
    }
}
