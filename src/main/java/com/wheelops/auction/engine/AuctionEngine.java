package com.wheelops.auction.engine;

import com.wheelops.auction.model.Allocation;
import com.wheelops.auction.model.AuctionResult;
import com.wheelops.auction.model.SearchTerm;
import com.wheelops.auction.model.Slot;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** Fixed auction flow; strategies supply only ranking and pricing behavior. */
public final class AuctionEngine {
    public AuctionResult run(SearchTerm term, List<BidSnapshot> bids, AuctionConfiguration configuration) {
        Objects.requireNonNull(term, "term");
        Objects.requireNonNull(bids, "bids");
        Objects.requireNonNull(configuration, "configuration");

        List<RankedBid> ranked = bids.stream()
                .filter(bid -> bid.remainingBudget().compareTo(bid.maxBid()) >= 0)
                .map(bid -> configuration.ranking().rank(bid, configuration))
                .sorted(Comparator.comparing(RankedBid::score).reversed()
                        .thenComparing(rankedBid -> rankedBid.bid().bidder()))
                .toList();

        int winners = Math.min(configuration.slotCount(), ranked.size());
        List<Allocation> allocations = IntStream.range(0, winners)
                .mapToObj(index -> new Allocation(
                        ranked.get(index).bid().bidder(),
                        new Slot(index + 1),
                        configuration.pricing().price(ranked, index, configuration.reserve())))
                .toList();
        return new AuctionResult(term, allocations);
    }
}
