package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.repository.BlockRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {

    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;

    @Value("${blockchain.difficulty.level}")
    private Integer difficulty;

    private String requiredPrefix;

    @PostConstruct
    public void init() {
        requiredPrefix = "0".repeat(difficulty);
    }

    @Override
    public boolean isChainValid() {
        List<Block> chain = blockRepository.findAll(Sort.by(Sort.Direction.ASC, "blockIndex"));

        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            String recalculatedHash = current.calculateHash();
            if (!current.getHash().equals(recalculatedHash)) {
                log.info("❌ Hash mismatch in block {}", current.getBlockIndex());
                log.info("Stored hash: {}", current.getHash());
                log.info("Recalculated: {}", recalculatedHash);
                return false;
            }

            if (!current.getPreviousHash().equals(previous.getHash())) {
                log.info("❌ Previous hash mismatch in block {}", current.getBlockIndex());
                return false;
            }
        }

        log.info("✅ Blockchain is valid");
        return true;
    }

    @Override
    public GenericResponse mineBlock() {
        return null;
    }

    @Override
    @Transactional
    public void createGenesisBlock() {
        if(blockRepository.count() != 0) {
            return;
        }

        Block genesisBlock = Block.builder()
                .blockIndex(0L)
                .isGenesis(true)
                .previousHash("0")
                .timestamp(new Date().getTime())
                .build();

        findHashAndSaveBlock(genesisBlock);
    }


    public void findHashAndSaveBlock(Block block) {

        Long nonce = 0L;
        String hash = "";
        while (!hash.startsWith(requiredPrefix)){
            block.setNonce(nonce);
            hash = block.calculateHash();
            nonce++;
        }

        block.setHash(hash);
        blockRepository.save(block);
    }




}