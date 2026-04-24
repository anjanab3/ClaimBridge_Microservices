package com.cts.claimbridge.util;

public enum Status {
    APPROVED,  // policy is accepted
    PENDING, // settlement in progress
    IN_REVIEW, // policy under process
    REJECTED // policy not approved
}