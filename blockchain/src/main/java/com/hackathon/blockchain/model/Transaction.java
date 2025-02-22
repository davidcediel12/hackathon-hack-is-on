package com.hackathon.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    private Long id;

    private String assetSymbol;
    private BigDecimal amount;
    private BigDecimal pricePerUnit;
    private String type;

    @CreatedDate
    private LocalDateTime timestamp;

    private String status;
    private BigDecimal fee;
    private Long senderWalletId;
    private Long receiverWalletId;


}
