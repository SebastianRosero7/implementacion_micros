import json
import os
from datetime import datetime, timezone

import pika

RABBITMQ_URL = os.getenv("RABBITMQ_URL")
EXCHANGE = "banco_preguntas.eventos"


class RabbitMQPublisher:
    """Publica eventos de dominio siguiendo el contrato de contracts/eventos.md."""

    def publicar(self, routing_key: str, evento: str, data: dict) -> None:
        mensaje = {
            "evento": evento,
            "version": 1,
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "data": data,
        }
        conexion = pika.BlockingConnection(pika.URLParameters(RABBITMQ_URL))
        canal = conexion.channel()
        canal.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
        canal.basic_publish(
            exchange=EXCHANGE,
            routing_key=routing_key,
            body=json.dumps(mensaje),
            properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        )
        conexion.close()