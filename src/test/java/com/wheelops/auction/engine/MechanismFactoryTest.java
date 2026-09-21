package com.wheelops.auction.engine;

import com.wheelops.auction.model.Money;
import com.wheelops.auction.pricing.FirstPrice;
import com.wheelops.auction.pricing.SecondPrice;
import com.wheelops.auction.ranking.RankByBid;
import com.wheelops.auction.ranking.RankByScore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MechanismFactoryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("mechanisms")
    void wiresEachRequiredMechanismToTheCorrectStrategies(
            AuctionMechanism mechanism,
            Class<?> ranking,
            Class<?> pricing) {
        AuctionConfiguration configuration = MechanismFactory.create(mechanism, 3, Money.ZERO, Map.of());

        assertInstanceOf(ranking, configuration.ranking());
        assertInstanceOf(pricing, configuration.pricing());
    }

    static Stream<Arguments> mechanisms() {
        return Stream.of(
                Arguments.of(AuctionMechanism.RANK_BY_BID_FIRST_PRICE, RankByBid.class, FirstPrice.class),
                Arguments.of(AuctionMechanism.RANK_BY_BID_SECOND_PRICE, RankByBid.class, SecondPrice.class),
                Arguments.of(AuctionMechanism.RANK_BY_SCORE_FIRST_PRICE, RankByScore.class, FirstPrice.class),
                Arguments.of(AuctionMechanism.RANK_BY_SCORE_SECOND_PRICE, RankByScore.class, SecondPrice.class));
    }
}
