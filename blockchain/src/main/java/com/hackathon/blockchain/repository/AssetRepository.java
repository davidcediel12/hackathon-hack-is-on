package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}