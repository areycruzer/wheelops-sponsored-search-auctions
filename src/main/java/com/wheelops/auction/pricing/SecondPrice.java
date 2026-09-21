package com.wheelops.auction.pricing;

import com.wheelops.auction.engine.RankedBid;
import com.wheelops.auction.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class SecondPrice implements PricingStrategy {
    @Override
    public Money price(List<RankedBid> ranked, int index, Money reserve) {
        RankedBid current = ranked.get(index);
        if (index + 1 == ranked.size()) {
            return reserve.min(current.bid().maxBid());
        }
        BigDecimal required = ranked.get(index + 1).score()
                .divide(current.weight(), 2, RoundingMode.HALF_EVEN);
        Money price = Money.ofCents(required.movePointRight(2).longValueExact());
        return price.min(current.bid().maxBid());
    }
}
