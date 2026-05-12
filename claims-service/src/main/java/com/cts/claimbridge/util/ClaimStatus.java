package com.cts.claimbridge.util;

public enum ClaimStatus {
    IN_COMING,          // claim request is made
    IN_REVIEW,          // under investigation
    PAYMENT_SCHEDULED,  // payment has been scheduled by payout officer
    SETTLED,            // payment process completed
    REJECTED,           // claim is not accepted
    CLOSED              // claim is completed or rejected
}
