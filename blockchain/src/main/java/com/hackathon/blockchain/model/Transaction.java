package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Transaction {

    @Id
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String status;
    private Double fee;


    @ManyToOne
    private Block block;




}
