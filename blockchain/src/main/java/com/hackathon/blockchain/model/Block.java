package com.hackathon.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Block {

    @Id
    private Long id;



    public String calculateHash(){
        return ""; // TODO change
    }

    public String getBlockIndex() {
        return null; // TODO change
    }

    public String getHash() {
        return null; // TODO change
    }

    public String getPreviousHash() {

        return null; // TODO change
    }
}
