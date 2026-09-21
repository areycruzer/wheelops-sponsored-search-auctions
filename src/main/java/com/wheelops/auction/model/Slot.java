package com.wheelops.auction.model;

/** One-based position in the sponsored result list. */
public record Slot(int position) {
    public Slot {
        if (position < 1) {
            throw new IllegalArgumentException("slot position must be positive");
        }
    }
}
