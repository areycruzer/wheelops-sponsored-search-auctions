package com.wheelops.auction.model;

import java.util.Objects;

/** Normalized keyword that selects an independent auction. */
public record SearchTerm(String value) {
    public SearchTerm {
        Objects.requireNonNull(value, "search term");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("search term cannot be blank");
        }
    }
}
