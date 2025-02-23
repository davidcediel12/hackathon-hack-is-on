package com.hackathon.blockchain.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue
    private Long id;

    private String symbol;
    private Double quantity;

    @Column(name = "purchase_price")
    private Double purchasedPrice;

    @ManyToOne
    private Wallet wallet;

}
