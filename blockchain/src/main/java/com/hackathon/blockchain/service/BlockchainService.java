package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final BlockRepository blockRepository;

    public boolean isChainValid() {
        List<Block> chain = blockRepository.findAll(Sort.by(Sort.Direction.ASC, "blockIndex"));

        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            String recalculatedHash = current.calculateHash();
            if (!current.getHash().equals(recalculatedHash)) {
                System.out.println("❌ Hash mismatch in block " + current.getBlockIndex());
                System.out.println("Stored hash: " + current.getHash());
                System.out.println("Recalculated: " + recalculatedHash);
                return false;
            }

            if (!current.getPreviousHash().equals(previous.getHash())) {
                System.out.println("❌ Previous hash mismatch in block " + current.getBlockIndex());
                return false;
            }
        }

        System.out.println("✅ Blockchain is valid");
        return true;
    }
}