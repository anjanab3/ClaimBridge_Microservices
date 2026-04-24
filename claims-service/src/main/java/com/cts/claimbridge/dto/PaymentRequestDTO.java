package com.cts.claimbridge.dto;

import com.cts.claimbridge.entity.Payment;

public class PaymentRequestDTO {

    Payment payment;

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
