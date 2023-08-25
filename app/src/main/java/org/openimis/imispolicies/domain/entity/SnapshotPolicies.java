package org.openimis.imispolicies.domain.entity;

public class SnapshotPolicies {

    private final int active;
    private final int expired;
    private final int idle;
    private final int suspended;

    public SnapshotPolicies(int active, int expired, int idle, int suspended) {
        this.active = active;
        this.expired = expired;
        this.idle = idle;
        this.suspended = suspended;
    }

    public int getActive() {
        return active;
    }

    public int getExpired() {
        return expired;
    }

    public int getIdle() {
        return idle;
    }

    public int getSuspended() {
        return suspended;
    }
}
