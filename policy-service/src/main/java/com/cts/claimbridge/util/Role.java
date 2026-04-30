package com.cts.claimbridge.util;

public enum Role {

    ADMIN("ADM"),
    USER("PH"),
    CLAIMS_INTAKE_AGENT("CI"),
    CLAIMS_ADJUSTER("CA"),
    FRAUD_ANALYST("FA"),
    UNDERWRITER("UW"),
    PAYOUT_OFFICER("PO"),
    AUDITOR("CO");

    private final String prefix;

    Role(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}