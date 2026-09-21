package com.wheelops.auction.ranking;

import com.wheelops.auction.engine.AuctionConfiguration;
import com.wheelops.auction.engine.BidSnapshot;

import java.math.BigDecimal;

public final class RankByScore implements RankingStrategy {
    @Override
    public BigDecimal weight(BidSnapshot bid, AuctionConfiguration configuration) {
        return configuration.requiredWeightFor(bid.bidder());
    }
}
