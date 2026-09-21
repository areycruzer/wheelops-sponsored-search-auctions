package com.wheelops.auction.model;

import java.util.Objects;

/** Result of attempting to charge a click against a prior auction result. */
public record ClickOutcome(ClickStatus status, Money amountCharged) {
    public ClickOutcome {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(amountCharged, "amount charged");
        if (status == ClickStatus.CHARGED && amountCharged.cents() < 0) {
            throw new IllegalArgumentException("charged amount cannot be negative");
        }
        if (status != ClickStatus.CHARGED && !amountCharged.equals(Money.ZERO)) {
            throw new IllegalArgumentException("uncharged click must report zero");
        }
    }

    public static ClickOutcome charged(Money amount) {
        return new ClickOutcome(ClickStatus.CHARGED, amount);
    }

    public static ClickOutcome notCharged(ClickStatus status) {
        return new ClickOutcome(status, Money.ZERO);
    }

    public boolean charged() {
        return status == ClickStatus.CHARGED;
    }
}
