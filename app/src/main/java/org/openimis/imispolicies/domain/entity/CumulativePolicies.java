package org.openimis.imispolicies.domain.entity;

public class CumulativePolicies {

    private final int newPolicies;
    private final int renewedPolicies;
    private final int expiredPolicies;
    private final int suspendedPolicies;
    private final double collectedContributions;

    public CumulativePolicies(int newPolicies, int renewedPolicies, int expiredPolicies, int suspendedPolicies, double collectedContributions) {
        this.newPolicies = newPolicies;
        this.renewedPolicies = renewedPolicies;
        this.expiredPolicies = expiredPolicies;
        this.suspendedPolicies = suspendedPolicies;
        this.collectedContributions = collectedContributions;
    }

    public int getNewPolicies() {
        return newPolicies;
    }

    public int getRenewedPolicies() {
        return renewedPolicies;
    }

    public int getExpiredPolicies() {
        return expiredPolicies;
    }

    public int getSuspendedPolicies() {
        return suspendedPolicies;
    }

    public double getCollectedContributions() {
        return collectedContributions;
    }
}
