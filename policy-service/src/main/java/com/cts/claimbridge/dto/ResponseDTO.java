package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseDTO {
    private String message;

    public ResponseDTO(String s) {
        this.message = s;
    }
}