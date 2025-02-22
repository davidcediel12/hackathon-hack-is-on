package com.hackathon.blockchain.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    private Integer purchasedPrice;

    @ManyToOne
    private Wallet wallet;

}
