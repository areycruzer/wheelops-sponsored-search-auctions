package com.wheelops.auction.model;

import java.util.Objects;

/** Stable, deterministic advertiser identifier. */
public record BidderId(String value) implements Comparable<BidderId> {
    public BidderId {
        value = normalized(value, "bidder id");
    }

    private static String normalized(String value, String label) {
        Objects.requireNonNull(value, label);
        String result = value.trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        return result;
    }

    @Override
    public int compareTo(BidderId other) {
        return value.compareTo(other.value);
    }
}
