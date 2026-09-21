package com.wheelops.auction.engine;

import com.wheelops.auction.model.Allocation;
import com.wheelops.auction.model.AuctionResult;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuctionEngineTest {
    private static final SearchTerm TERM = new SearchTerm("shoes");
    private final AuctionEngine engine = new AuctionEngine();

    @Test
    void filtersIneligibleBidsBeforeRanking() {
        AuctionConfiguration configuration = MechanismFactory.create(
                AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Money.ZERO, Map.of());

        AuctionResult result = engine.run(TERM, List.of(
                bid("A", 500, 499),
                bid("B", 300, 300)), configuration);

        assertEquals(List.of(new BidderId("B")), result.allocations().stream().map(Allocation::bidder).toList());
    }

    @Test
    void assignsTheConfiguredNumberOfSlotsWithAStableTieBreak() {
        AuctionConfiguration configuration = MechanismFactory.create(
                AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 2, Money.ZERO, Map.of());

        AuctionResult result = engine.run(TERM, List.of(
                bid("B", 500, 500),
                bid("A", 500, 500),
                bid("C", 400, 400)), configuration);

        assertEquals(List.of(new BidderId("A"), new BidderId("B")),
                result.allocations().stream().map(Allocation::bidder).toList());
    }

    @Test
    void pricesAWinningSlotAgainstTheFirstLosingBidder() {
        AuctionConfiguration configuration = MechanismFactory.create(
                AuctionMechanism.RANK_BY_BID_SECOND_PRICE, 1, Money.ZERO, Map.of());

        AuctionResult result = engine.run(TERM, List.of(
                bid("A", 500, 500),
                bid("B", 300, 300)), configuration);

        assertEquals(Money.ofCents(300), result.allocations().get(0).pricePerClick());
    }

    private static BidSnapshot bid(String bidder, long maxBid, long budget) {
        return new BidSnapshot(new BidderId(bidder), TERM, Money.ofCents(maxBid), Money.ofCents(budget));
    }
}
