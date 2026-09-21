package com.wheelops.auction.pricing;

import com.wheelops.auction.engine.RankedBid;
import com.wheelops.auction.model.Money;

import java.util.List;

public sealed interface PricingStrategy permits FirstPrice, SecondPrice {
    Money price(List<RankedBid> ranked, int index, Money reserve);
}
