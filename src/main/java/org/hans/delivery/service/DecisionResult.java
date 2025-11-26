package org.hans.delivery.service;

public record DecisionResult(
        boolean accepted,
        String reason
) {
}
