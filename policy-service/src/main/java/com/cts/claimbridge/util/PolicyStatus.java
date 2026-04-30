package com.cts.claimbridge.util;

public enum PolicyStatus {
    ACTIVE, // valid policy
    EXPIRED, // claim date is over
    SUSPENDED, // policy under hold
    CANCELLED // policy not existed
}
