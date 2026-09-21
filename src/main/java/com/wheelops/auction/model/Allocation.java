package com.wheelops.auction.model;

import java.util.Objects;

/** Immutable price quote for a bidder that won one displayed slot. */
public record Allocation(BidderId bidder, Slot slot, Money pricePerClick) {
    public Allocation {
        Objects.requireNonNull(bidder, "bidder");
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(pricePerClick, "price per click");
    }
}
