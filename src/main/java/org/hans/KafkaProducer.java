package org.hans;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class KafkaProducer {

    // 1. Inyecta el Emitter asociado al canal 'events-out' definido en properties
    @Channel("events-out")
    Emitter<String> eventEmitter;

    private AtomicInteger counter = new AtomicInteger();

    // 2. Envía un mensaje cada 5 segundos @Scheduled(every = "5s")
    public void sendEvent() {
        String message = "Evento de Quarkus #" + counter.incrementAndGet() + " - Hora: " + java.time.LocalTime.now();

        // 3. Usa el Emitter para enviar el mensaje al canal de Kafka
        eventEmitter.send(message);

        System.out.println("-> Enviado: " + message);
    }
}