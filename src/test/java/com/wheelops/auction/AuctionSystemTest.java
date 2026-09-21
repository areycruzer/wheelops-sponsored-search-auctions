package com.wheelops.auction;

import com.wheelops.auction.engine.AuctionMechanism;
import com.wheelops.auction.engine.AuctionSystem;
import com.wheelops.auction.engine.MechanismFactory;
import com.wheelops.auction.model.Allocation;
import com.wheelops.auction.model.AuctionResult;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.ClickStatus;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import com.wheelops.auction.model.Slot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionSystemTest {
    private static final BidderId A = new BidderId("A");
    private static final BidderId B = new BidderId("B");
    private static final BidderId C = new BidderId("C");
    private static final BidderId D = new BidderId("D");

    @ParameterizedTest(name = "{0}")
    @MethodSource("workedExamples")
    void implementsAllFourRequiredMechanisms(String name, Scenario scenario) {
        AuctionSystem system = auction(scenario.mechanism(), scenario.slotCount(), scenario.weights());
        scenario.bids().forEach(bid -> system.register(bid.bidder(), scenario.term(), bid.maxBid(), bid.budget()));

        AuctionResult result = system.search(scenario.term());

        assertEquals(scenario.expectedBidders(), result.allocations().stream().map(Allocation::bidder).toList());
        assertEquals(scenario.expectedPrices(), result.allocations().stream().map(Allocation::pricePerClick).toList());
        for (Allocation allocation : result.allocations()) {
            Money maxBid = scenario.bids().stream()
                    .filter(bid -> bid.bidder().equals(allocation.bidder()))
                    .findFirst()
                    .orElseThrow()
                    .maxBid();
            assertTrue(allocation.pricePerClick().compareTo(maxBid) <= 0);
        }
    }

    static Stream<Arguments> workedExamples() {
        SearchTerm shoes = new SearchTerm("shoes");
        List<Bid> shoeBids = List.of(
                bid(A, 500), bid(B, 300), bid(C, 200), bid(D, 100));
        SearchTerm flights = new SearchTerm("flights");
        List<Bid> flightBids = List.of(
                bid(A, 400), bid(B, 300), bid(C, 1_000));
        Map<BidderId, BigDecimal> flightWeights = Map.of(
                A, decimal("1.0"), B, decimal("2.0"), C, decimal("0.5"));

        return Stream.of(
                Arguments.of("rank by bid + GFP", new Scenario(
                        AuctionMechanism.RANK_BY_BID_FIRST_PRICE, shoes, 3, Map.of(), shoeBids,
                        List.of(A, B, C), List.of(money(500), money(300), money(200)))),
                Arguments.of("rank by bid + GSP", new Scenario(
                        AuctionMechanism.RANK_BY_BID_SECOND_PRICE, shoes, 3, Map.of(), shoeBids,
                        List.of(A, B, C), List.of(money(300), money(200), money(100)))),
                Arguments.of("rank by score + GFP", new Scenario(
                        AuctionMechanism.RANK_BY_SCORE_FIRST_PRICE, flights, 3, flightWeights, flightBids,
                        List.of(B, C, A), List.of(money(300), money(1_000), money(400)))),
                Arguments.of("rank by score + GSP", new Scenario(
                        AuctionMechanism.RANK_BY_SCORE_SECOND_PRICE, flights, 3, flightWeights, flightBids,
                        List.of(B, C, A), List.of(money(250), money(800), Money.ZERO))));
    }

    @Test
    void chargesOnlyOnClickThenExcludesAnExhaustedBidder() {
        SearchTerm flights = new SearchTerm("flights");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_SCORE_SECOND_PRICE, 3, Map.of(
                A, decimal("1.0"), B, decimal("2.0"), C, decimal("0.5")));
        system.register(A, flights, money(400), money(1_000));
        system.register(B, flights, money(300), money(1_000));
        system.register(C, flights, money(1_000), money(1_000));

        AuctionResult firstResult = system.search(flights);
        assertEquals(money(1_000), remaining(system, B, flights));
        for (int click = 0; click < 4; click++) {
            assertEquals(ClickStatus.CHARGED, system.click(firstResult, new Slot(1)).status());
        }

        assertEquals(Money.ZERO, remaining(system, B, flights));
        assertEquals(List.of(C, A), system.search(flights).allocations().stream()
                .map(Allocation::bidder)
                .toList());
    }

    @Test
    void displayWithoutClickDoesNotChangeBudget() {
        SearchTerm term = new SearchTerm("shoes");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Map.of());
        system.register(A, term, money(500), money(1_000));

        system.search(term);

        assertEquals(money(1_000), remaining(system, A, term));
    }

    @Test
    void ordersEqualScoresByAscendingBidderId() {
        SearchTerm term = new SearchTerm("tires");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_SCORE_FIRST_PRICE, 2, Map.of(
                A, decimal("1"), B, decimal("1")));
        system.register(B, term, money(500), money(500));
        system.register(A, term, money(500), money(500));

        assertEquals(List.of(A, B), system.search(term).allocations().stream()
                .map(Allocation::bidder)
                .toList());
    }

    @Test
    void handlesOneBidderFewerBiddersThanSlotsAndTheZeroLastSlotPrice() {
        SearchTerm term = new SearchTerm("tires");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_SECOND_PRICE, 3, Map.of());
        system.register(A, term, money(500), money(500));

        AuctionResult result = system.search(term);

        assertEquals(1, result.allocations().size());
        assertEquals(Money.ZERO, result.allocationAt(new Slot(1)).orElseThrow().pricePerClick());
        assertTrue(result.allocationAt(new Slot(2)).isEmpty());
        assertEquals(ClickStatus.SLOT_NOT_FOUND, system.click(result, new Slot(2)).status());
    }

    @Test
    void returnsAnEmptyResultForAnUnregisteredOrIneligibleTerm() {
        SearchTerm unregistered = new SearchTerm("unregistered");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Map.of());
        assertTrue(system.search(unregistered).allocations().isEmpty());

        system.register(A, unregistered, money(500), money(499));
        assertTrue(system.search(unregistered).allocations().isEmpty());
    }

    @Test
    void changesRankAndEligibilityWhenBidsAndBudgetsAreUpdated() {
        SearchTerm term = new SearchTerm("flowers");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 2, Map.of());
        system.register(A, term, money(500), money(1_000));
        system.register(B, term, money(300), money(1_000));
        assertEquals(A, system.search(term).allocations().get(0).bidder());

        system.updateBid(B, term, money(600));
        assertEquals(B, system.search(term).allocations().get(0).bidder());

        system.updateBudget(B, term, money(599));
        assertEquals(A, system.search(term).allocations().get(0).bidder());
    }

    @Test
    void isolatesBidsAndBudgetsByKeyword() {
        SearchTerm shoes = new SearchTerm("shoes");
        SearchTerm flights = new SearchTerm("flights");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Map.of());
        system.register(A, shoes, money(500), money(1_000));
        system.register(A, flights, money(300), money(700));

        assertEquals(ClickStatus.CHARGED, system.click(system.search(shoes), new Slot(1)).status());

        assertEquals(money(500), remaining(system, A, shoes));
        assertEquals(money(700), remaining(system, A, flights));
        assertEquals(A, system.search(flights).allocations().get(0).bidder());
    }

    @Test
    void keepsOneActiveBidPerBidderAndTerm() {
        SearchTerm term = new SearchTerm("shoes");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Map.of());
        system.register(A, term, money(500), money(500));

        assertThrows(IllegalArgumentException.class, () -> system.register(A, term, money(600), money(600)));
    }

    @Test
    void rejectsAStaleClickIfBudgetWasReducedAfterTheSearch() {
        SearchTerm term = new SearchTerm("shoes");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Map.of());
        system.register(A, term, money(500), money(1_000));
        AuctionResult shown = system.search(term);
        system.updateBudget(A, term, money(400));

        assertEquals(ClickStatus.INSUFFICIENT_BUDGET, system.click(shown, new Slot(1)).status());
        assertEquals(money(400), remaining(system, A, term));
    }

    @Test
    void roundsGspOnlyOnceUsingHalfEvenCents() {
        SearchTerm term = new SearchTerm("rounding");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_SCORE_SECOND_PRICE, 2, Map.of(
                A, decimal("2"), B, decimal("1")));
        system.register(A, term, money(100), money(100));
        system.register(B, term, money(3), money(3));

        AuctionResult result = system.search(term);

        assertEquals(money(2), result.allocationAt(new Slot(1)).orElseThrow().pricePerClick());
        assertEquals(Money.ZERO, result.allocationAt(new Slot(2)).orElseThrow().pricePerClick());
    }

    @Test
    void rejectsInvalidDomainValuesAndMissingScoreWeights() {
        assertThrows(IllegalArgumentException.class, () -> new BidderId(" "));
        assertThrows(IllegalArgumentException.class, () -> new SearchTerm(" "));
        assertThrows(IllegalArgumentException.class, () -> new Slot(0));
        assertThrows(IllegalArgumentException.class, () -> money(-1));

        SearchTerm term = new SearchTerm("weighted");
        AuctionSystem system = auction(AuctionMechanism.RANK_BY_SCORE_FIRST_PRICE, 1, Map.of());
        system.register(A, term, money(100), money(100));
        assertThrows(IllegalArgumentException.class, () -> system.search(term));
    }

    private static AuctionSystem auction(
            AuctionMechanism mechanism,
            int slots,
            Map<BidderId, BigDecimal> weights) {
        return new AuctionSystem(MechanismFactory.create(mechanism, slots, Money.ZERO, weights));
    }

    private static Bid bid(BidderId bidder, long maxBid) {
        return new Bid(bidder, money(maxBid), money(10_000));
    }

    private static Money money(long cents) {
        return Money.ofCents(cents);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private static Money remaining(AuctionSystem system, BidderId bidder, SearchTerm term) {
        return system.remainingBudget(bidder, term).orElseThrow();
    }

    private record Bid(BidderId bidder, Money maxBid, Money budget) {
    }

    private record Scenario(
            AuctionMechanism mechanism,
            SearchTerm term,
            int slotCount,
            Map<BidderId, BigDecimal> weights,
            List<Bid> bids,
            List<BidderId> expectedBidders,
            List<Money> expectedPrices) {
    }
}
