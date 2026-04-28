package com.cts.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseDTO {
    private String message;

    public ResponseDTO(String message) {
        this.message = message;
    }
}
