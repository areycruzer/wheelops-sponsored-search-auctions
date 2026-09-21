package com.wheelops.auction.engine;

import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;

import java.util.Objects;

/** Read-only bid state used for one auction evaluation. */
public record BidSnapshot(BidderId bidder, SearchTerm term, Money maxBid, Money remainingBudget) {
    public BidSnapshot {
        Objects.requireNonNull(bidder, "bidder");
        Objects.requireNonNull(term, "term");
        Objects.requireNonNull(maxBid, "max bid");
        Objects.requireNonNull(remainingBudget, "remaining budget");
        if (maxBid.equals(Money.ZERO)) {
            throw new IllegalArgumentException("max bid must be positive");
        }
    }
}
