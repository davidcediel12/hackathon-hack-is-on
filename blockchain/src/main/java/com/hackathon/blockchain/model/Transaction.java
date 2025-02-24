package com.hackathon.blockchain.model;

import com.hackathon.blockchain.dto.response.TransactionDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String status;
    private Double fee;


    @ManyToOne
    private Block block;


    public TransactionDto toDto() {

        return new TransactionDto(id, assetSymbol, amount, pricePerUnit, type,
                timestamp, status, fee, senderWallet.getId(), receiverWallet.getId());
    }


}
