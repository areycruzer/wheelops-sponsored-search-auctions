package com.wheelops.auction.engine;

import com.wheelops.auction.model.Allocation;
import com.wheelops.auction.model.AuctionResult;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.ClickOutcome;
import com.wheelops.auction.model.ClickStatus;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import com.wheelops.auction.model.Slot;

import java.util.Objects;
import java.util.Optional;

/** Application service for registration, search, dynamic updates, and click charging. */
public final class AuctionSystem {
    private final AuctionConfiguration configuration;
    private final AuctionEngine engine = new AuctionEngine();
    private final BidRepository bids = new BidRepository();

    public AuctionSystem(AuctionConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public void register(BidderId bidder, SearchTerm term, Money maxBid, Money budget) {
        bids.register(bidder, term, maxBid, budget);
    }

    public void updateBid(BidderId bidder, SearchTerm term, Money maxBid) {
        bids.updateBid(bidder, term, maxBid);
    }

    public void updateBudget(BidderId bidder, SearchTerm term, Money budget) {
        bids.updateBudget(bidder, term, budget);
    }

    public AuctionResult search(SearchTerm term) {
        Objects.requireNonNull(term, "term");
        return engine.run(term, bids.snapshotsFor(term), configuration);
    }

    public ClickOutcome click(AuctionResult result, Slot slot) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(slot, "slot");
        Optional<Allocation> allocation = result.allocationAt(slot);
        if (allocation.isEmpty()) {
            return ClickOutcome.notCharged(ClickStatus.SLOT_NOT_FOUND);
        }
        Allocation winner = allocation.get();
        return bids.find(winner.bidder(), result.term())
                .filter(bid -> bid.charge(winner.pricePerClick()))
                .map(ignored -> ClickOutcome.charged(winner.pricePerClick()))
                .orElseGet(() -> ClickOutcome.notCharged(ClickStatus.INSUFFICIENT_BUDGET));
    }

    public Optional<Money> remainingBudget(BidderId bidder, SearchTerm term) {
        return bids.find(bidder, term).map(BidRepository.MutableBid::remainingBudget);
    }
}
