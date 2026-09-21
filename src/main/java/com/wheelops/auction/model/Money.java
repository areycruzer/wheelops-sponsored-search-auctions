package com.wheelops.auction.model;

import java.math.BigDecimal;

/** A non-negative amount represented exactly in cents. */
public record Money(long cents) implements Comparable<Money> {
    public static final Money ZERO = new Money(0);

    public Money {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public BigDecimal asDecimal() {
        return BigDecimal.valueOf(cents, 2);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    public Money minus(Money other) {
        return new Money(Math.subtractExact(cents, other.cents));
    }

    public Money min(Money other) {
        return compareTo(other) <= 0 ? this : other;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(cents, other.cents);
    }
}
