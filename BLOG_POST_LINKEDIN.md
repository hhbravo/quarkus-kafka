# Arquitectura Orientada a Eventos con Quarkus y Kafka
 En sistemas distribuidos, nos enfrentamos constantemente a desafíos de escalabilidad, desacoplamiento y resiliencia. Tradicionalmente, los microservicios se comunican mediante APIs REST síncronas, lo que puede generar acoplamiento, puntos únicos de fallo y dificultades para escalar independientemente.

En este contexto, surge una pregunta fundamental: ¿Cómo podemos construir sistemas que sean verdaderamente desacoplados, escalables y resilientes? La respuesta está en una arquitectura basada en eventos.

Arquitectura Orientada a Eventos (EDA)

"La Arquitectura Orientada a Eventos es un patrón arquitectónico donde los componentes del sistema se comunican mediante la producción y consumo de eventos. Un evento representa un cambio de estado o una acción significativa que otros componentes pueden observar y reaccionar de forma asíncrona".

Esta arquitectura permite que los microservicios se comuniquen sin conocerse directamente, mejorando el desacoplamiento, la escalabilidad y la resiliencia del sistema.

Para implementar esta arquitectura, necesitamos una plataforma que permita publicar, almacenar y consumir eventos de manera eficiente. Aquí es donde Apache Kafka juega un papel fundamental.

Apache Kafka

"Apache Kafka es una plataforma distribuida de streaming de eventos de código abierto diseñada para manejar grandes volúmenes de datos en tiempo real. Permite publicar y suscribirse a streams de eventos, almacenarlos de forma duradera y procesarlos según sea necesario".

Kafka actúa como el sistema nervioso de nuestra arquitectura, permitiendo que los microservicios se comuniquen de forma asíncrona mediante topics (tópicos), donde los productores publican eventos y los consumidores los procesan.

Sin embargo, integrar Kafka en aplicaciones Java tradicionales puede ser complejo. Requiere configurar productores y consumidores manualmente, manejar la serialización, gestionar conexiones y lidiar con la complejidad de la programación reactiva. Aquí es donde Quarkus simplifica significativamente este proceso.

Quarkus Reactive Messaging

"Quarkus es un framework Java nativo de Kubernetes optimizado para GraalVM y HotSpot, diseñado para aplicaciones cloud-native. Su extensión SmallRye Reactive Messaging proporciona integración declarativa con Kafka mediante anotaciones simples".

Con Quarkus, podemos integrar Kafka de forma declarativa usando anotaciones como `@Incoming` y `@Channel`, reduciendo significativamente la complejidad del código y permitiendo un desarrollo más rápido y mantenible.

Implementación Práctica: Sistema de Órdenes y Delivery

Para demostrar estos conceptos, implementé una Prueba de Concepto (PoC) con dos microservicios que se comunican mediante eventos:

- **orders-service**: Microservicio que recibe solicitudes REST para crear órdenes, las persiste en su base de datos y publica eventos cuando se crea una nueva orden.

- **delivery-service**: Microservicio que consume eventos de creación de órdenes, valida el inventario contra su propia base de datos y publica eventos con el resultado de la validación.

La comunicación entre estos servicios es completamente asíncrona y bidireccional: orders → delivery → orders, todo mediante Kafka.

Contenido del artículo

Arquitectura del Sistema

En el diagrama podemos observar cómo los microservicios se comunican exclusivamente mediante Kafka, sin dependencias directas entre ellos. Cada servicio mantiene su propia base de datos, lo que permite escalabilidad y evolución independiente.

Flujo de Eventos

El flujo completo funciona de la siguiente manera:

1. Un cliente realiza una petición POST a orders-service para crear una orden.
2. orders-service persiste la orden en su base de datos con estado "PENDING" y publica un evento `OrderCreatedEvent` en el topic `createOrderService`.
3. delivery-service consume el evento, crea la orden en su propia base de datos (si no existe), valida el inventario y actualiza el estado (ACCEPTED o DENIED).
4. delivery-service publica un evento `OrderValidatedEvent` en el topic `orderValidated`.
5. orders-service consume el evento de validación y actualiza el estado de la orden en su base de datos.

Este flujo es completamente asíncrono, lo que significa que la respuesta al cliente no espera la validación del inventario, mejorando la experiencia del usuario y la capacidad de respuesta del sistema.

Configuración con Quarkus

Una de las ventajas más significativas de usar Quarkus es la simplicidad de configuración. Para publicar eventos, solo necesitamos:

```properties
mp.messaging.outgoing.order-create-out.connector=smallrye-kafka
mp.messaging.outgoing.order-create-out.topic=createOrderService
mp.messaging.outgoing.order-create-out.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

Y en el código Java:

```java
@ApplicationScoped
public class OrderProducer {
    @Inject
    @Channel("order-create-out")
    Emitter<String> emitter;

    public void send(Order order) {
        OrderCreatedEvent event = orderMapper.toCreatedEvent(order);
        String payload = objectMapper.writeValueAsString(event);
        emitter.send(payload);
    }
}
```

Para consumir eventos, la configuración es igualmente simple:

```properties
mp.messaging.incoming.order-create-in.connector=smallrye-kafka
mp.messaging.incoming.order-create-in.topic=createOrderService
mp.messaging.incoming.order-create-in.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Y en el código:

```java
@ApplicationScoped
public class DeliveryConsumer {
    @Incoming("order-create-in")
    @Transactional
    public void consume(String payload) {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        // Procesar evento...
    }
}
```

Con estas anotaciones, Quarkus maneja automáticamente la conexión con Kafka, la serialización/deserialización, el manejo de errores y la gestión del ciclo de vida de los consumidores.

Bases de Datos Separadas

Un aspecto crucial de esta arquitectura es que cada microservicio mantiene su propia base de datos:

- **orders-service**: `jdbc:h2:./target/h2db/orders`
- **delivery-service**: `jdbc:h2:./target/h2db/delivery`

Esta separación permite:

- **Independencia**: Cada servicio puede evolucionar su esquema sin afectar al otro
- **Escalabilidad**: Cada base de datos puede escalarse independientemente según la carga
- **Resiliencia**: Fallos en una base de datos no afectan directamente a la otra
- **Sincronización eventual**: Los estados se sincronizan mediante eventos, no mediante transacciones distribuidas

Ventajas de esta Arquitectura

1. **Desacoplamiento Real**: Los servicios no tienen dependencias directas entre sí, solo se comunican mediante eventos.

2. **Escalabilidad Horizontal**: Cada servicio puede escalarse independientemente según su carga específica. Si delivery-service necesita más recursos para validar inventario, podemos escalarlo sin afectar orders-service.

3. **Resiliencia**: Si un servicio falla temporalmente, los eventos se almacenan en Kafka y se procesarán cuando el servicio se recupere.

4. **Extensibilidad**: Nuevos servicios pueden suscribirse a eventos existentes sin modificar los servicios que los publican.

5. **Alto Rendimiento**: Quarkus ofrece tiempos de arranque ultrarrápidos (menos de 1 segundo en modo dev) y bajo consumo de memoria, ideal para entornos cloud-native.

Consideraciones Importantes

Aunque esta arquitectura ofrece grandes ventajas, también presenta desafíos que debemos considerar:

- **Sincronización de Estado**: Mantener consistencia entre bases de datos separadas requiere estrategias de idempotencia y manejo de eventos duplicados.

- **Manejo de Errores**: Es crucial implementar estrategias de reintento, dead-letter queues y circuit breakers para manejar eventos fallidos.

- **Testing**: Probar flujos asíncronos requiere herramientas específicas y puede ser más complejo que probar APIs síncronas.

- **Trazabilidad**: Es importante implementar distributed tracing para rastrear eventos a través de múltiples servicios.

Conclusión

La arquitectura orientada a eventos con Quarkus y Kafka proporciona una solución poderosa para construir sistemas distribuidos escalables, resilientes y desacoplados. Quarkus simplifica significativamente la integración con Kafka mediante su enfoque declarativo, reduciendo la complejidad del código y acelerando el desarrollo.

Esta arquitectura es especialmente adecuada para sistemas que requieren:

- Alta disponibilidad y escalabilidad
- Bajo acoplamiento entre componentes
- Procesamiento asíncrono
- Integración con sistemas legacy o externos
- Capacidad de evolucionar independientemente

En mi experiencia implementando esta PoC, pude validar que Quarkus facilita una implementación rápida y eficiente de arquitecturas orientadas a eventos, gracias a su integración nativa con Kafka, configuración simple y alto rendimiento. La combinación de Quarkus y Kafka permite construir sistemas modernos que pueden crecer y adaptarse a las necesidades del negocio.

Si estás considerando implementar una arquitectura orientada a eventos o tienes preguntas sobre cómo integrar Kafka con Quarkus, estaré encantado de compartir más detalles y experiencias sobre este tema.

Referencias

https://quarkus.io/guides/kafka

https://kafka.apache.org/documentation/

https://smallrye.io/smallrye-reactive-messaging/

https://martinfowler.com/articles/201701-event-driven.html

https://microservices.io/patterns/data/event-driven-architecture.html

