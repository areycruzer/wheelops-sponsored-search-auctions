package com.wheelops.auction.engine;

import java.math.BigDecimal;
import java.util.Objects;

/** Bid with the ranking weight and score used to order an auction. */
public record RankedBid(BidSnapshot bid, BigDecimal weight, BigDecimal score) {
    public RankedBid {
        Objects.requireNonNull(bid, "bid");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(score, "score");
        if (weight.signum() <= 0 || score.signum() < 0) {
            throw new IllegalArgumentException("weight must be positive and score cannot be negative");
        }
    }
}
