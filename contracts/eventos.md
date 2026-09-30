# Contrato de Eventos de Dominio — RabbitMQ

Exchange sugerido: `banco_preguntas.eventos` (tipo `topic`)

---

## 1. PreguntaCreada

**Publicado por:** MS-1 Banco de Preguntas (Sebas)
**Consumido por:** MS-2 Ciclo de Vida y Revisión (Habi) → crea el registro de Revisión inicial (CU-05)
**Routing key:** `pregunta.creada`
**Cola sugerida:** `ciclo_vida.pregunta_creada`

```json
{
  "evento": "PreguntaCreada",
  "version": 1,
  "timestamp": "2026-09-30T14:32:00Z",
  "data": {
    "pregunta_id": "b3f1c2a4-1234-4a5b-9c1d-9f0e8a7b6c5d",
    "autor_id": "u-001",
    "competencia": "Lectura crítica",
    "tema": "Comprensión de lectura",
    "subtema": "Inferencias textuales",
    "nivel_dificultad": "MEDIO",
    "estado": "PENDIENTE_REVISION"
  }
}
```

**Notas:**
- No se envía el contenido completo de la pregunta (contexto, distractores, etc.) para no acoplar el evento al detalle interno de MS-1. Si Ciclo de Vida necesita el detalle completo, lo pide vía gRPC (`ObtenerPreguntaCompleta`) usando `pregunta_id`.
- `estado` se incluye porque MS-2 valida que solo cree la Revisión si la pregunta ya pasó la validación estructural.

---

## 2. PreguntaAprobada / PreguntaRechazada

**Publicado por:** MS-2 Ciclo de Vida y Revisión (Habi)
**Consumido por:** MS-1 Banco de Preguntas (Sebas) → actualiza el estado de la Pregunta (CU-08 / CU-09)
**Routing key:** `pregunta.aprobada` o `pregunta.rechazada`
**Cola sugerida:** `banco_preguntas.resultado_revision`

```json
{
  "evento": "PreguntaAprobada",
  "version": 1,
  "timestamp": "2026-09-30T16:10:00Z",
  "data": {
    "pregunta_id": "b3f1c2a4-1234-4a5b-9c1d-9f0e8a7b6c5d",
    "revision_id": "r-045",
    "revisor_id": "u-020",
    "resultado": "APROBADA",
    "observaciones": "Cumple todos los criterios de calidad, lista para publicar."
  }
}
```

Para el caso de rechazo, mismo esquema cambiando `evento` a `"PreguntaRechazada"`, `resultado` a `"RECHAZADA"` y routing key a `pregunta.rechazada` — las observaciones en ese caso deben explicar el motivo.

---

## Convenciones comunes

- **`version`**: permite evolucionar el esquema sin romper al consumidor (si cambian campos, se sube a `2` y el consumidor decide cómo manejar ambas).
- **`timestamp`**: en UTC, formato ISO 8601.
- **Idempotencia**: cada consumidor debe verificar si ya procesó ese `pregunta_id` + `evento` (ej. guardando el id del mensaje) antes de aplicar el cambio, por si RabbitMQ reentrega el mensaje.
- **Serialización**: JSON plano en el `body` del mensaje AMQP, con `content_type: application/json` en las propiedades del mensaje.
