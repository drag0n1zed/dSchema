package io.github.drag0n1zed.dschema.building;

public enum BuildResult {
    COMPLETED,
    PARTIAL,
    CANCELED; // state inconsistent or become idle

    public boolean isSuccess() {
        return this == COMPLETED || this == PARTIAL;
    }

}
