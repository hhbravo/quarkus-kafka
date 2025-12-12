# Post LinkedIn - Implementación de Arquitectura Orientada a Eventos con Quarkus y Kafka

---

🚀 **Acabo de completar una Prueba de Concepto (PoC) implementando una arquitectura orientada a eventos con Quarkus y Apache Kafka, y quiero compartir los aprendizajes clave.**

El objetivo era crear un sistema donde dos microservicios independientes se comunicaran de forma asíncrona mediante eventos, manteniendo bases de datos separadas y desacoplamiento completo.

**La arquitectura implementada:**

✅ **orders-service**: Recibe solicitudes REST, crea órdenes y publica eventos cuando se registra una nueva orden

✅ **stock-service**: Consume eventos de creación, valida inventario y publica eventos de validación

✅ **Comunicación bidireccional**: orders → stock → orders mediante Kafka

**Lo que más me impresionó:**

🔥 **Quarkus Reactive Messaging** hace que integrar Kafka sea increíblemente simple. Con solo anotaciones como `@Incoming` y `@Channel`, tienes un sistema de eventos funcionando en minutos.

💡 **Desacoplamiento real**: Cada servicio tiene su propia base de datos H2, evoluciona independientemente y puede escalarse según su carga específica.

⚡ **Alto rendimiento**: Quarkus ofrece tiempos de arranque ultrarrápidos y bajo consumo de memoria, perfecto para entornos cloud-native.

**Flujo completo del sistema:**

1️⃣ Cliente crea orden → orders-service persiste y publica `OrderCreatedEvent`

2️⃣ stock-service consume el evento, valida inventario y actualiza su BD

3️⃣ stock-service publica `OrderValidatedEvent` con resultado (ACCEPTED/DENIED)

4️⃣ orders-service consume el evento y actualiza el estado de la orden

**Tecnologías utilizadas:**
• Quarkus 3.15.7
• Apache Kafka
• SmallRye Reactive Messaging
• H2 Database
• Hibernate ORM + Panache

**Aprendizajes clave:**

🎯 La arquitectura orientada a eventos permite escalabilidad horizontal sin límites

🎯 Las bases de datos separadas por servicio mejoran la resiliencia y mantenibilidad

🎯 La serialización JSON con Jackson es simple pero poderosa para eventos

🎯 La idempotencia es crucial cuando trabajas con eventos asíncronos

**Próximos pasos que me gustaría explorar:**
• Saga Pattern para transacciones distribuidas
• Dead Letter Queue para manejo de errores
• Distributed Tracing con Jaeger
• Event Sourcing para auditoría completa

¿Has trabajado con arquitecturas orientadas a eventos? ¿Qué patrones o tecnologías recomiendas para sistemas distribuidos?

#Quarkus #ApacheKafka #EventDrivenArchitecture #Microservices #Java #ReactiveProgramming #SoftwareArchitecture #CloudNative #TechBlog #SoftwareEngineering

---

**Versión alternativa más corta (si LinkedIn limita caracteres):**

---

🚀 **PoC completada: Arquitectura orientada a eventos con Quarkus y Kafka**

Implementé un sistema donde dos microservicios se comunican mediante eventos Kafka, manteniendo bases de datos separadas.

**Arquitectura:**
• orders-service: Crea órdenes y publica eventos
• stock-service: Valida inventario y publica resultados
• Comunicación bidireccional: orders → stock → orders

**Highlights:**
✅ Quarkus Reactive Messaging simplifica Kafka con anotaciones
✅ Desacoplamiento real con BD separadas por servicio
✅ Alto rendimiento y escalabilidad horizontal

**Flujo:**
1. Cliente crea orden → orders publica evento
2. Stock valida inventario → publica resultado
3. Orders actualiza estado según validación

**Stack:** Quarkus 3.15.7 | Kafka | SmallRye Reactive Messaging | H2 | Panache

¿Experiencias con arquitecturas orientadas a eventos? ¿Qué patrones recomiendan?

#Quarkus #Kafka #EventDrivenArchitecture #Microservices #Java #SoftwareArchitecture

---

