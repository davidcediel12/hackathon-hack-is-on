package com.hackathon.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Block {

    @Id
    @GeneratedValue
    private Long id;

    private Long blockIndex;


    private String previousBlockHash;

    @Timestamp
    LocalDateTime timestamp;

    private Long nonce;

    private String hash;

    private Boolean isGenesis;

    @OneToMany(mappedBy = "block")
    Set<Transaction> transactions;


    public String calculateHash(){
        return ""; // TODO change
    }

    public String getPreviousHash() {

        return null; // TODO change
    }
}
