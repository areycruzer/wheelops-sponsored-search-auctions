package com.wheelops.auction.engine;

import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.pricing.FirstPrice;
import com.wheelops.auction.pricing.SecondPrice;
import com.wheelops.auction.ranking.RankByBid;
import com.wheelops.auction.ranking.RankByScore;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Maps each required auction mechanism to its ranking and pricing strategies. */
public final class MechanismFactory {
    private MechanismFactory() {
    }

    public static AuctionConfiguration create(
            AuctionMechanism mechanism,
            int slotCount,
            Money reserve,
            Map<BidderId, BigDecimal> weights) {
        Objects.requireNonNull(mechanism, "mechanism");
        return switch (mechanism) {
            case RANK_BY_BID_FIRST_PRICE -> new AuctionConfiguration(
                    new RankByBid(), new FirstPrice(), slotCount, reserve, weights);
            case RANK_BY_BID_SECOND_PRICE -> new AuctionConfiguration(
                    new RankByBid(), new SecondPrice(), slotCount, reserve, weights);
            case RANK_BY_SCORE_FIRST_PRICE -> new AuctionConfiguration(
                    new RankByScore(), new FirstPrice(), slotCount, reserve, weights);
            case RANK_BY_SCORE_SECOND_PRICE -> new AuctionConfiguration(
                    new RankByScore(), new SecondPrice(), slotCount, reserve, weights);
        };
    }
}
