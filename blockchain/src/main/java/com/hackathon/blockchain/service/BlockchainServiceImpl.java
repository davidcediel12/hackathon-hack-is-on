package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {

    private final BlockRepository blockRepository;

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







}