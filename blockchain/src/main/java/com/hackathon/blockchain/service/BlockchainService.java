package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.GenericResponse;

public interface BlockchainService {
    boolean isChainValid();


    GenericResponse mineBlock();

    void createGenesisBlock();
}
