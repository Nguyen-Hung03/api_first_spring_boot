package com.example.shop.entity;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED;

    public Set<OrderStatus> allowedNextStatuses() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(SHIPPING, CANCELLED);
            case SHIPPING -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNextStatuses().contains(next);
    }
}
