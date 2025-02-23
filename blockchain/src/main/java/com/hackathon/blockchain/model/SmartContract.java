package com.hackathon.blockchain.model;

import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class SmartContract {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String conditionExpression;
    private String action;
    private Double actionValue;
    private Long issuerWalletId;
    @Lob
    private String digitalSignature;
    private String status;

    public ContractResponse toResponse(){
        return new ContractResponse(name, conditionExpression,
                action, actionValue, issuerWalletId, digitalSignature);
    }

    public Contract toDto(){
        return new Contract(name, conditionExpression, action, actionValue, issuerWalletId);
    }

}
