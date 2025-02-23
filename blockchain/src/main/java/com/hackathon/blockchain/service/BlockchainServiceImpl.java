package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.repository.BlockRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.hackathon.blockchain.utils.MessageConstants.NO_BLOCKS_TO_MINE;

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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GenericResponse mineBlock() {
        List<Transaction> pendingTransactions = transactionRepository.findByStatus("PENDING");

        if(pendingTransactions.isEmpty()) {
            throw new ApiException(NO_BLOCKS_TO_MINE, HttpStatus.BAD_REQUEST);
        }
        Block previousBlock = blockRepository.findAll(
                        PageRequest.of(0, 1,
                                Sort.by(Sort.Direction.DESC, "blockIndex")))
                .get()
                .findFirst()
                .orElseThrow(() -> new ApiException("No blocks available", HttpStatus.INTERNAL_SERVER_ERROR));


        Block newBlock = Block.builder()
                .blockIndex(previousBlock.getBlockIndex() + 1)
                .isGenesis(false)
                .previousHash(previousBlock.getHash())
                .timestamp(new Date().getTime())
                .transactions(new HashSet<>(pendingTransactions))
                .build();

        findHashAndSaveBlock(newBlock);

        for (Transaction transaction : pendingTransactions) {
            transaction.setStatus("MINED");
        }
        transactionRepository.saveAll(pendingTransactions);

        return new GenericResponse("Block mined: " + newBlock.getHash());
    }

    @Override
    @Transactional
    public void createGenesisBlock() {
        if (blockRepository.count() != 0) {
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
        while (!hash.startsWith(requiredPrefix)) {
            block.setNonce(nonce);
            hash = block.calculateHash();
            nonce++;
        }

        block.setHash(hash);
        blockRepository.save(block);
    }


}