# Taller 2 — Implementación de Microservicios con DDD

Sistema de Gestión de Preguntas para las Pruebas Saber Pro — Arquitectura
de Microservicios, Universidad del Cauca.

## Cómo levantar el proyecto completo

### Requisitos
- Docker Desktop
- Python 3.11+ (para MS-1)
- Java 21 + Maven (para MS-2)

Si tienes RabbitMQ instalado como servicio nativo en tu sistema,
detenlo antes de continuar — compite por el puerto `5672` con el
contenedor de Docker.

### 1. Levantar infraestructura y REST de ambos microservicios
```bash
docker-compose up -d --build
```
Verifica que estén arriba:
```bash
docker ps
```

### 2. Levantar el servidor gRPC de MS-1
Por ahora corre como proceso separado (no containerizado):
```bash
cd ms-banco-preguntas
python -m app.interfaces.grpc.server
```

### 3. Probar

| Servicio | URL |
|---|---|
| REST MS-1 | http://localhost:8001/docs |
| REST MS-2 | http://localhost:8002 |
| gRPC MS-1 | localhost:50051 |
| RabbitMQ UI | http://localhost:15672 (guest/guest) |
