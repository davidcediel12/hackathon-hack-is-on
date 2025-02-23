package com.hackathon.blockchain.service.contract;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;

public interface ContractService {


    ContractResponse createContract(Contract contract);
    GenericResponse validateContract(Long contractId);
}
