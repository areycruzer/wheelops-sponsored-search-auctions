package com.wheelops.auction.pricing;

import com.wheelops.auction.engine.RankedBid;
import com.wheelops.auction.model.Money;

import java.util.List;

public final class FirstPrice implements PricingStrategy {
    @Override
    public Money price(List<RankedBid> ranked, int index, Money reserve) {
        return ranked.get(index).bid().maxBid();
    }
}
