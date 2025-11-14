package org.hans.orders.producer;


import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    @Inject
    @Channel("events-out")
    Emitter<String> emitter;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void sendCreateOrderEventAsync(String payload) {
        executor.submit(() -> {
            try {
                log.debug("Sending event payloadSize={}", payload.length());
                emitter.send(payload).whenComplete((s, t) -> {
                    if (t != null) log.error("Failed to send event", t);
                    else log.info("Event sent");
                }).toCompletableFuture().join();
            } catch (Exception e) {
                log.error("Unexpected error sending kafka event", e);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down OrderProducer executor");
        executor.shutdownNow();
    }
}