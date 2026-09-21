package com.wheelops.auction.engine;

import com.wheelops.auction.model.BidderId;
import com.wheelops.auction.model.Money;
import com.wheelops.auction.model.SearchTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Private bidder-owned bids plus the keyword index used to run auctions. */
final class BidRepository {
    private final Map<BidderId, BidderAccount> accounts = new HashMap<>();
    private final Map<SearchTerm, Map<BidderId, MutableBid>> bidsByTerm = new HashMap<>();

    synchronized void register(BidderId bidder, SearchTerm term, Money maxBid, Money budget) {
        Objects.requireNonNull(bidder, "bidder");
        Objects.requireNonNull(term, "term");
        requirePositive(maxBid, "max bid");
        Objects.requireNonNull(budget, "budget");

        BidderAccount account = accounts.computeIfAbsent(bidder, BidderAccount::new);
        if (account.bids.containsKey(term)) {
            throw new IllegalArgumentException("bidder already has a bid for this term");
        }
        MutableBid bid = new MutableBid(bidder, term, maxBid, budget);
        account.bids.put(term, bid);
        bidsByTerm.computeIfAbsent(term, ignored -> new HashMap<>()).put(bidder, bid);
    }

    synchronized void updateBid(BidderId bidder, SearchTerm term, Money maxBid) {
        requirePositive(maxBid, "max bid");
        findRequired(bidder, term).setMaxBid(maxBid);
    }

    synchronized void updateBudget(BidderId bidder, SearchTerm term, Money budget) {
        findRequired(bidder, term).setRemainingBudget(Objects.requireNonNull(budget, "budget"));
    }

    synchronized List<BidSnapshot> snapshotsFor(SearchTerm term) {
        return bidsByTerm.getOrDefault(term, Map.of()).values().stream()
                .map(MutableBid::snapshot)
                .toList();
    }

    synchronized Optional<MutableBid> find(BidderId bidder, SearchTerm term) {
        BidderAccount account = accounts.get(bidder);
        return account == null ? Optional.empty() : Optional.ofNullable(account.bids.get(term));
    }

    private MutableBid findRequired(BidderId bidder, SearchTerm term) {
        return find(bidder, term).orElseThrow(
                () -> new IllegalArgumentException("no bid registered for bidder and term"));
    }

    private static void requirePositive(Money value, String label) {
        Objects.requireNonNull(value, label);
        if (value.equals(Money.ZERO)) {
            throw new IllegalArgumentException(label + " must be positive");
        }
    }

    private static final class BidderAccount {
        private final Map<SearchTerm, MutableBid> bids = new HashMap<>();

        private BidderAccount(BidderId ignored) {
        }
    }

    static final class MutableBid {
        private final BidderId bidder;
        private final SearchTerm term;
        private final AtomicLong remainingBudgetCents;
        private volatile Money maxBid;

        private MutableBid(BidderId bidder, SearchTerm term, Money maxBid, Money budget) {
            this.bidder = bidder;
            this.term = term;
            this.maxBid = maxBid;
            this.remainingBudgetCents = new AtomicLong(budget.cents());
        }

        private void setMaxBid(Money maxBid) {
            this.maxBid = maxBid;
        }

        private void setRemainingBudget(Money budget) {
            remainingBudgetCents.set(budget.cents());
        }

        private BidSnapshot snapshot() {
            return new BidSnapshot(bidder, term, maxBid, Money.ofCents(remainingBudgetCents.get()));
        }

        Money remainingBudget() {
            return Money.ofCents(remainingBudgetCents.get());
        }

        boolean charge(Money price) {
            long cents = price.cents();
            long current;
            do {
                current = remainingBudgetCents.get();
                if (current < cents) {
                    return false;
                }
            } while (!remainingBudgetCents.compareAndSet(current, current - cents));
            return true;
        }
    }
}
