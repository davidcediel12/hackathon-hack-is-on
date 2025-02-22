package com.hackathon.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class SmartContract {

    @Id
    private Long id;

    private String name;
    private String conditionExpression;
    private String action;
    private Double actionValue;
    private Long issuerWalletId;
    private String digitalSignature;

}
