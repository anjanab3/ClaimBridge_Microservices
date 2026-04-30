package com.cts.claimbridge.util;

public enum ClaimStatus {
    IN_COMING, // claim request is made
    IN_REVIEW, // under investigation
    SETTLED, // payment process completed
    REJECTED, // claim is not accepted
    CLOSED // claim is completed or rejected
}
