package com.hackathon.blockchain.dto.response;

import java.util.List;

public record TransactionsDto(List<TransactionDto> sent,
                              List<TransactionDto> received) {
}
