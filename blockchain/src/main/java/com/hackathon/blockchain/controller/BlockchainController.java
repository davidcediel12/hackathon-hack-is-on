package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.response.BlockDto;
import com.hackathon.blockchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;


    @PostMapping("/mine")
    public ResponseEntity<GenericResponse> mine(){
        return ResponseEntity.ok(blockchainService.mineBlock());
    }

    @GetMapping
    public ResponseEntity<List<BlockDto>> getBlockchain(){
        return ResponseEntity.ok(blockchainService.getBlockchain());
    }

    @GetMapping("/validate")
    public ResponseEntity<GenericResponse> validate(){
        GenericResponse validityMessage = new GenericResponse("Blockchain valid: " + blockchainService.isChainValid());
        return ResponseEntity.ok(validityMessage);
    }
}
