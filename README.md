# quarkus-kafka

Este proyecto es una **Prueba de Concepto (PoC)** que implementa un flujo de procesamiento basado en eventos utilizando **Quarkus** y **Apache Kafka**. 

## 📋 Descripción

Implementación de una arquitectura orientada a eventos (Event-Driven Architecture) con dos microservicios independientes que se comunican mediante Kafka, manteniendo bases de datos separadas y desacoplamiento completo.

## 🏗️ Arquitectura

El proyecto está estructurado como un **proyecto multi-módulo Maven** con dos microservicios independientes:

- **orders-service**: Microservicio encargado de registrar órdenes. 
  - Recibe solicitudes REST para crear órdenes
  - Publica eventos `OrderCreatedEvent` cuando se crea una orden
  - Consume eventos `OrderValidatedEvent` para actualizar el estado de las órdenes
  
- **delivery-service**: Microservicio que procesa órdenes para delivery.
  - Consume eventos de creación de órdenes desde Kafka
  - Realiza validación de inventario
  - Actualiza su propia base de datos
  - Publica eventos `OrderValidatedEvent` con el resultado de la validación

## Estructura del Proyecto

```
quarkus-kafka/
├── pom.xml                    # POM padre (agregador)
├── orders-service/            # Módulo del servicio de órdenes
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/org/hans/orders/
│           └── resources/
│               └── application.properties
└── delivery-service/          # Módulo del servicio de delivery
    ├── pom.xml
    └── src/
        └── main/
            ├── java/org/hans/delivery/
            └── resources/
                └── application.properties
```

## Requisitos Previos

- Java 21+
- Maven 3.8+
- Docker y Docker Compose (para Kafka)

## Configuración

### Base de Datos

Cada microservicio tiene su propia base de datos H2 independiente:
- **orders-service**: `jdbc:h2:./target/h2db/orders`
- **delivery-service**: `jdbc:h2:./target/h2db/delivery`

Esta separación permite que cada servicio mantenga su propio estado y sea completamente independiente.

### Kafka

Inicia Kafka usando Docker Compose:

```bash
docker-compose up -d
```

Esto iniciará Zookeeper y Kafka en los puertos 2181 y 9092 respectivamente.

## Ejecución

### Compilar todos los módulos

Para compilar todos los módulos desde la raíz:

**Windows:**
```bash
.\mvnw.cmd clean install
```

**Linux/Mac:**
```bash
./mvnw clean install
```

**⚠️ Importante:** 
- Usa siempre el **Maven Wrapper** (`mvnw` o `mvnw.cmd`) en lugar de `mvn` directamente
- Si usas **Java 25**, agrega `-Dnet.bytebuddy.experimental=true` (ya incluido en los scripts)

**Compilar sin tests:**

**Windows:**
```bash
.\mvnw.cmd clean install -DskipTests -Dnet.bytebuddy.experimental=true
```

**Linux/Mac:**
```bash
./mvnw clean install -DskipTests -Dnet.bytebuddy.experimental=true
```

**Nota sobre Java 25:** El proyecto está configurado para Java 21. Si usas Java 25, se requiere el modo experimental de Byte Buddy. Se recomienda usar Java 21 (LTS) para mejor compatibilidad.

### Ejecutar servicios desde la raíz del proyecto

Puedes ejecutar cada servicio directamente desde el proyecto raíz usando perfiles Maven:

#### Opción 1: Usando Scripts (Más fácil)

**Windows:**

Terminal 1 - orders-service:
```bash
.\run-orders.bat
```

Terminal 2 - delivery-service:
```bash
.\run-delivery.bat
```

**Linux/Mac:**

Terminal 1 - orders-service:
```bash
chmod +x run-orders.sh
./run-orders.sh
```

Terminal 2 - delivery-service:
```bash
chmod +x run-delivery.sh
./run-delivery.sh
```

#### Opción 2: Desde la Raíz del Proyecto

**Ejecutar orders-service:**

Windows:
```bash
.\mvnw.cmd quarkus:dev -pl orders-service
```

Linux/Mac:
```bash
./mvnw quarkus:dev -pl orders-service
```

El servicio estará disponible en `http://localhost:8080`

**Ejecutar delivery-service:**

En una terminal separada:

Windows:
```bash
.\mvnw.cmd quarkus:dev -pl delivery-service -Dquarkus.http.port=8081
```

Linux/Mac:
```bash
./mvnw quarkus:dev -pl delivery-service -Dquarkus.http.port=8081
```

**Nota**: El delivery-service está configurado para usar el puerto 8081 por defecto para evitar conflictos con orders-service.

### Ejecutar servicios desde sus directorios

Alternativamente, puedes ejecutar cada servicio desde su propio directorio:

#### orders-service

**Windows:**
```bash
cd orders-service
..\mvnw.cmd quarkus:dev
```

**Linux/Mac:**
```bash
cd orders-service
../mvnw quarkus:dev
```

#### delivery-service

**Windows:**
```bash
cd delivery-service
..\mvnw.cmd quarkus:dev -Dquarkus.http.port=8081
```

**Linux/Mac:**
```bash
cd delivery-service
../mvnw quarkus:dev -Dquarkus.http.port=8081
```

**Nota**: Cada servicio tiene su propia base de datos, por lo que ambos pueden tener `quarkus.hibernate-orm.database.generation=drop-and-create` activado sin conflictos.

## Endpoints

### orders-service

- `POST /orders/create` - Crea una nueva orden y publica un evento `OrderCreatedEvent`

Ejemplo de request:
```json
{
  "client": {
    "name": "Juan Pérez",
    "address": "Calle 123",
    "phoneNumber": "123456789",
    "documentNumber": "12345678",
    "documentType": "DNI"
  },
  "items": [
    {
      "productId": 1,
      "name": "item1",
      "quantity": 2,
      "additionalInformation": "Información adicional"
    }
  ],
  "total": 100.0
}
```

## Flujo de Eventos

1. **orders-service** recibe una petición para crear una orden
2. La orden se persiste en la base de datos de orders-service con estado `PENDING`
3. Se publica un evento `OrderCreatedEvent` en el topic `createOrderService`
4. **delivery-service** consume el evento del topic `createOrderService`
5. El servicio de delivery crea la orden en su propia base de datos (si no existe)
6. El servicio de delivery valida el inventario
7. Se actualiza el estado de la orden en la base de datos de delivery-service (ACCEPTED o DENIED)
8. Se publica un evento `OrderValidatedEvent` en el topic `orderValidated`
9. **orders-service** consume el evento del topic `orderValidated`
10. Se actualiza el estado de la orden en la base de datos de orders-service (ACCEPTED o DENIED)

## Topics de Kafka

- `createOrderService`: Topic donde se publican los eventos de creación de órdenes
- `orderValidated`: Topic donde se publican los eventos de validación de órdenes

## Desarrollo

### Modo Desarrollo

Cada servicio puede ejecutarse en modo desarrollo con recarga automática:

```bash
# Terminal 1 - orders-service
cd orders-service
../mvnw quarkus:dev

# Terminal 2 - delivery-service
cd delivery-service
../mvnw quarkus:dev
```

### Compilación

Para compilar todos los módulos:

```bash
./mvnw clean package
```

Para compilar un módulo específico:

```bash
cd orders-service
../mvnw clean package
```

## Base de Datos H2 Console

Ambos servicios exponen la consola H2 en:
- orders-service: `http://localhost:8080/h2-console`
- delivery-service: `http://localhost:8080/h2-console` (si se ejecuta en el mismo puerto, usar un puerto diferente)

JDBC URL: `jdbc:h2:./target/h2db/orders` (o `delivery` según el servicio)
Usuario: `sa`
Contraseña: (vacía)

## 🛠️ Tecnologías Utilizadas

- **Quarkus 3.15.7**: Framework Java nativo de Kubernetes
- **Apache Kafka**: Plataforma de streaming de eventos
- **SmallRye Reactive Messaging**: Integración reactiva con Kafka
- **H2 Database**: Base de datos en memoria para desarrollo
- **Hibernate ORM + Panache**: ORM simplificado
- **RESTEasy Reactive**: Framework REST reactivo
- **Jackson**: Serialización/deserialización JSON
- **Maven**: Gestión de dependencias y construcción
- **Java 21**: Lenguaje de programación

## 📚 Documentación Adicional

Para una explicación detallada de la arquitectura, implementación y conceptos, consulta el documento **[BLOG_POST.md](BLOG_POST.md)** que incluye:

- Explicación detallada de Event-Driven Architecture
- Diagramas de arquitectura y flujo de eventos
- Detalles de implementación técnica
- Resultados y aprendizajes
- Referencias y recursos adicionales

## 📝 Notas Importantes

- ✅ Ambos servicios están diseñados para ser **módulos independientes** y pueden ejecutarse por separado
- ✅ Cada servicio tiene su **propia base de datos independiente** (H2)
- ✅ El módulo delivery **crea automáticamente** las órdenes en su base de datos cuando recibe eventos si no existen
- ✅ El módulo orders **actualiza automáticamente** el estado de las órdenes cuando recibe eventos de validación desde delivery-service
- ✅ El flujo completo es **asíncrono y basado en eventos**, permitiendo que los servicios se escalen independientemente
- ✅ Los eventos se serializan/deserializan como **JSON** usando Jackson
- ✅ Se implementa **idempotencia básica** para evitar procesamiento duplicado

## 🎯 Características Principales

- **Arquitectura Orientada a Eventos**: Comunicación asíncrona mediante Kafka
- **Microservicios Desacoplados**: Sin dependencias directas entre servicios
- **Bases de Datos Separadas**: Cada servicio mantiene su propio estado
- **Validación de Inventario**: Lógica de negocio distribuida
- **Comunicación Bidireccional**: Flujo completo de eventos (orders → delivery → orders)
- **Configuración Declarativa**: Uso de anotaciones y properties para Kafka

## 🚀 Próximos Pasos

Para mejorar esta PoC, se podrían implementar:

- [ ] Saga Pattern para transacciones distribuidas
- [ ] Dead Letter Queue para eventos fallidos
- [ ] Event Sourcing para auditoría completa
- [ ] Circuit Breaker para resiliencia
- [ ] Distributed Tracing (Jaeger/Zipkin)
- [ ] Métricas y Monitoreo (Prometheus/Grafana)
- [ ] Schema Registry para versionado de eventos
- [ ] Tests de integración con Testcontainers
