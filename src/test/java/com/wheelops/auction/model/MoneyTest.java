package com.wheelops.auction.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {
    @Test
    void representsExactNonNegativeCents() {
        assertEquals(Money.ZERO, Money.ofCents(0));
        assertEquals(new BigDecimal("5.00"), Money.ofCents(500).asDecimal());
        assertThrows(IllegalArgumentException.class, () -> Money.ofCents(-1));
    }

    @Test
    void supportsExactArithmeticAndOrdering() {
        Money oneDollar = Money.ofCents(100);
        Money twoDollars = Money.ofCents(200);

        assertEquals(Money.ofCents(300), oneDollar.plus(twoDollars));
        assertEquals(oneDollar, Money.ofCents(300).minus(twoDollars));
        assertEquals(oneDollar, oneDollar.min(twoDollars));
        assertTrue(oneDollar.compareTo(twoDollars) < 0);
        assertThrows(IllegalArgumentException.class, () -> oneDollar.minus(twoDollars));
    }
}
