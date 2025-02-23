package com.hackathon.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Wallet senderWallet;

    @ManyToOne(optional = false)
    private Wallet receiverWallet;

    private String assetSymbol;
    private Double amount;
    private Double pricePerUnit;
    private String type;

    @CreationTimestamp
    private OffsetDateTime timestamp;

    private String status;
    private Double fee;


    @ManyToOne
    private Block block;




}
