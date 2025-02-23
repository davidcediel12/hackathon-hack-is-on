package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;
import com.hackathon.blockchain.service.contract.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class SmartContractController {

    private final ContractService contractService;


    @PostMapping("/create")
    public ResponseEntity<ContractResponse> createContract(@RequestBody @Valid Contract contractRequest) {
        return ResponseEntity.ok(contractService.createContract(contractRequest));
    }

    @GetMapping("/validate/{contractId}")
    public ResponseEntity<GenericResponse> validateContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.validateContract(contractId));
    }
}
