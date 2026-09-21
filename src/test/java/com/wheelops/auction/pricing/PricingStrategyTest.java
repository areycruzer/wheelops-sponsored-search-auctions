package com.wheelops.auction.pricing;

import com.wheelops.auction.engine.BidSnapshot;
import com.wheelops.auction.engine.RankedBid;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingStrategyTest {
    @Test
    void firstPriceChargesTheWinnerOwnBid() {
        Money price = new FirstPrice().price(List.of(ranked("A", 200, "1", "2")), 0, Money.ZERO);

        assertEquals(Money.ofCents(200), price);
    }

    @Test
    void secondPriceUsesTheNextScoreAndCurrentWeight() {
        Money price = new SecondPrice().price(List.of(
                ranked("A", 300, "2", "6"),
                ranked("B", 500, "1", "5")), 0, Money.ZERO);

        assertEquals(Money.ofCents(250), price);
    }

    @Test
    void secondPriceUsesAndCapsTheReserveForTheLastBidder() {
        RankedBid winner = ranked("A", 100, "1", "1");

        assertEquals(Money.ofCents(50), new SecondPrice().price(List.of(winner), 0, Money.ofCents(50)));
        assertEquals(Money.ofCents(100), new SecondPrice().price(List.of(winner), 0, Money.ofCents(999)));
    }

    @ParameterizedTest(name = "{0} / 2 rounds to {1} cents")
    @MethodSource("halfEvenCases")
    void secondPriceRoundsToCentsUsingHalfEven(String nextBidCents, long expectedCents) {
        Money price = new SecondPrice().price(List.of(
                ranked("A", 100, "2", "2"),
                ranked("B", Long.parseLong(nextBidCents), "1", "0.0" + nextBidCents)), 0, Money.ZERO);

        assertEquals(Money.ofCents(expectedCents), price);
    }

    static Stream<Arguments> halfEvenCases() {
        return Stream.of(
                Arguments.of("3", 2L),
                Arguments.of("5", 2L),
                Arguments.of("7", 4L));
    }

    private static RankedBid ranked(String bidder, long maxBidCents, String weight, String score) {
        return new RankedBid(
                new BidSnapshot(new BidderId(bidder), new SearchTerm("shoes"),
                        Money.ofCents(maxBidCents), Money.ofCents(maxBidCents)),
                new BigDecimal(weight),
                new BigDecimal(score));
    }
}
