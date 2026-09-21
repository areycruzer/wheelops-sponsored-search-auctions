package com.wheelops.auction.engine;

import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.pricing.PricingStrategy;
import com.wheelops.auction.ranking.RankingStrategy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Immutable auctioneer choices, fixed for the lifetime of an auction system. */
public record AuctionConfiguration(
        RankingStrategy ranking,
        PricingStrategy pricing,
        int slotCount,
        Money reserve,
        Map<BidderId, BigDecimal> weights) {

    public AuctionConfiguration {
        Objects.requireNonNull(ranking, "ranking");
        Objects.requireNonNull(pricing, "pricing");
        Objects.requireNonNull(reserve, "reserve");
        Objects.requireNonNull(weights, "weights");
        if (slotCount < 1) {
            throw new IllegalArgumentException("slot count must be positive");
        }
        weights = Map.copyOf(weights);
        for (Map.Entry<BidderId, BigDecimal> entry : weights.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "weight bidder");
            if (Objects.requireNonNull(entry.getValue(), "weight").signum() <= 0) {
                throw new IllegalArgumentException("weights must be positive");
            }
        }
    }

    public BigDecimal requiredWeightFor(BidderId bidder) {
        BigDecimal weight = weights.get(bidder);
        if (weight == null) {
            throw new IllegalArgumentException("missing weight for bidder " + bidder.value());
        }
        return weight;
    }
}
