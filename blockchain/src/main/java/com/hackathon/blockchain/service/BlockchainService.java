package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.response.BlockDto;

import java.util.List;

public interface BlockchainService {
    boolean isChainValid();


    GenericResponse mineBlock();

    void createGenesisBlock();

    List<BlockDto> getBlockchain();
}
