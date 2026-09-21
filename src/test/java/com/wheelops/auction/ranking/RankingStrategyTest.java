package com.wheelops.auction.ranking;

import com.wheelops.auction.engine.AuctionConfiguration;
import com.wheelops.auction.engine.BidSnapshot;
import com.wheelops.auction.engine.RankedBid;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import com.wheelops.auction.pricing.FirstPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RankingStrategyTest {
    private static final BidderId BIDDER = new BidderId("advertiser");
    private static final BidSnapshot BID = new BidSnapshot(
            BIDDER, new SearchTerm("shoes"), Money.ofCents(500), Money.ofCents(500));

    @Test
    void rankByBidAlwaysUsesWeightOne() {
        RankedBid ranked = new RankByBid().rank(BID, configuration(new RankByBid(), Map.of()));

        assertEquals(BigDecimal.ONE, ranked.weight());
        assertEquals(new BigDecimal("5.00"), ranked.score());
    }

    @Test
    void rankByScoreUsesTheAuctioneerConfiguredWeight() {
        RankedBid ranked = new RankByScore().rank(BID,
                configuration(new RankByScore(), Map.of(BIDDER, new BigDecimal("2.5"))));

        assertEquals(new BigDecimal("2.5"), ranked.weight());
        assertEquals(new BigDecimal("12.500"), ranked.score());
    }

    @Test
    void rankByScoreRejectsMissingWeights() {
        assertThrows(IllegalArgumentException.class,
                () -> new RankByScore().rank(BID, configuration(new RankByScore(), Map.of())));
    }

    private static AuctionConfiguration configuration(
            RankingStrategy ranking,
            Map<BidderId, BigDecimal> weights) {
        return new AuctionConfiguration(ranking, new FirstPrice(), 1, Money.ZERO, weights);
    }
}
