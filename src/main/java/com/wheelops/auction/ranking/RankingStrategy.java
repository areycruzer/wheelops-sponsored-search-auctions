package com.wheelops.auction.ranking;

import com.wheelops.auction.engine.AuctionConfiguration;
import com.wheelops.auction.engine.BidSnapshot;
import com.wheelops.auction.engine.RankedBid;

import java.math.BigDecimal;

public sealed interface RankingStrategy permits RankByBid, RankByScore {
    BigDecimal weight(BidSnapshot bid, AuctionConfiguration configuration);

    default RankedBid rank(BidSnapshot bid, AuctionConfiguration configuration) {
        BigDecimal weight = weight(bid, configuration);
        return new RankedBid(bid, weight, weight.multiply(bid.maxBid().asDecimal()));
    }
}
