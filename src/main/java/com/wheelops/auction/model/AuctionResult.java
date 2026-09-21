package com.wheelops.auction.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Immutable sponsored-result page for one keyword search. */
public record AuctionResult(SearchTerm term, List<Allocation> allocations) {
    public AuctionResult {
        Objects.requireNonNull(term, "term");
        allocations = List.copyOf(allocations);
        Set<BidderId> bidders = new HashSet<>();
        Set<Slot> slots = new HashSet<>();
        for (Allocation allocation : allocations) {
            if (!bidders.add(allocation.bidder()) || !slots.add(allocation.slot())) {
                throw new IllegalArgumentException("allocations must have unique bidders and slots");
            }
        }
    }

    public Optional<Allocation> allocationAt(Slot slot) {
        return allocations.stream().filter(allocation -> allocation.slot().equals(slot)).findFirst();
    }

    public Optional<Allocation> allocationFor(BidderId bidder) {
        return allocations.stream().filter(allocation -> allocation.bidder().equals(bidder)).findFirst();
    }
}
