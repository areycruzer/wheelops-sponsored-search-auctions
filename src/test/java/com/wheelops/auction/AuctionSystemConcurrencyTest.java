package com.wheelops.auction;

import com.wheelops.auction.engine.AuctionMechanism;
import com.wheelops.auction.engine.AuctionSystem;
import com.wheelops.auction.engine.MechanismFactory;
import com.wheelops.auction.model.AuctionResult;
import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.ClickOutcome;
import com.wheelops.auction.model.ClickStatus;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;
import com.wheelops.auction.model.Slot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionSystemConcurrencyTest {
    @Test
    void concurrentClicksNeverOverrunTheBudget() throws Exception {
        BidderId bidder = new BidderId("A");
        SearchTerm term = new SearchTerm("shoes");
        AuctionSystem system = new AuctionSystem(MechanismFactory.create(
                AuctionMechanism.RANK_BY_BID_FIRST_PRICE, 1, Money.ZERO, Map.of()));
        system.register(bidder, term, Money.ofCents(100), Money.ofCents(10_000));
        AuctionResult result = system.search(term);

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<ClickOutcome>> clicks = new ArrayList<>();
            for (int attempt = 0; attempt < 200; attempt++) {
                clicks.add(executor.submit(() -> system.click(result, new Slot(1))));
            }

            long charged = 0;
            long insufficient = 0;
            for (Future<ClickOutcome> click : clicks) {
                if (click.get().status() == ClickStatus.CHARGED) {
                    charged++;
                } else {
                    insufficient++;
                }
            }

            assertEquals(100, charged);
            assertEquals(100, insufficient);
            assertEquals(Money.ZERO, system.remainingBudget(bidder, term).orElseThrow());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.isShutdown());
        }
    }
}
