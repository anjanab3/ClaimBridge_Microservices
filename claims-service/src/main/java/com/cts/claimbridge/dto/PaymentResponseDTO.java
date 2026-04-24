package com.cts.claimbridge.dto;

import com.cts.claimbridge.entity.Payment;
import lombok.Data;

@Data
public class PaymentResponseDTO {

    Payment payment;

    private String status;

    private int statusCode;

}
