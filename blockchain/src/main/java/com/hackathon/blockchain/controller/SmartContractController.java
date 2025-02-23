package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;
import com.hackathon.blockchain.service.contract.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class SmartContractController {

    private final ContractService contractService;


    @PostMapping("/create")
    public ResponseEntity<ContractResponse> createContract(@RequestBody @Valid Contract contractRequest){
        return ResponseEntity.ok(contractService.createContract(contractRequest));
    }
}
