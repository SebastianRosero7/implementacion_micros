import json
import logging
import os
import threading
import time
import pika

from app.application.use_cases.gestionar_pregunta import GestionarPregunta
from app.domain.services.validador_estructural import ValidadorEstructural
from app.infrastructure.db.repositorio_pregunta_sqlalchemy import RepositorioPreguntaSQLAlchemy
from app.infrastructure.db.session import SessionLocal
from app.infrastructure.messaging.rabbitmq_publisher import RabbitMQPublisher

logger = logging.getLogger("RabbitMQConsumer")
logger.setLevel(logging.INFO)

RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/")
EXCHANGE = "banco_preguntas.eventos"
QUEUE = "banco_preguntas.resultado_revision"
ROUTING_KEYS = ["pregunta.aprobada", "pregunta.rechazada"]


def _procesar_mensaje(ch, method, properties, body):
    try:
        mensaje = json.loads(body.decode("utf-8"))
        evento = mensaje.get("evento")
        data = mensaje.get("data", {})
        pregunta_id_str = data.get("pregunta_id")
        resultado = data.get("resultado")

        if not resultado:
            if evento == "PreguntaAprobada":
                resultado = "APROBADA"
            elif evento == "PreguntaRechazada":
                resultado = "RECHAZADA"

        if not pregunta_id_str or not resultado:
            logger.warning(f"Mensaje descartado por formato incompleto: {mensaje}")
            ch.basic_ack(delivery_tag=method.delivery_tag)
            return

        pregunta_id = int(pregunta_id_str)
        logger.info(f"Procesando {evento} para pregunta ID={pregunta_id} con resultado={resultado}")

        db = SessionLocal()
        try:
            repo = RepositorioPreguntaSQLAlchemy(db)
            caso = GestionarPregunta(
                repositorio=repo,
                validador=ValidadorEstructural(),
                publisher=RabbitMQPublisher(),
            )
            pregunta = caso.registrar_resultado_revision(pregunta_id, resultado)
            logger.info(f"Pregunta {pregunta.id} actualizada exitosamente a estado {pregunta.estado.value}")
        finally:
            db.close()

        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        logger.error(f"Error procesando mensaje RabbitMQ: {e}", exc_info=True)
        # Confirmamos el mensaje para evitar que quede bloqueando la cola si es inválido
        ch.basic_ack(delivery_tag=method.delivery_tag)


def iniciar_consumidor():
    while True:
        try:
            logger.info(f"Conectando consumidor RabbitMQ a {RABBITMQ_URL}...")
            params = pika.URLParameters(RABBITMQ_URL)
            conexion = pika.BlockingConnection(params)
            canal = conexion.channel()

            canal.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
            canal.queue_declare(queue=QUEUE, durable=True)

            for key in ROUTING_KEYS:
                canal.queue_bind(exchange=EXCHANGE, queue=QUEUE, routing_key=key)

            canal.basic_qos(prefetch_count=1)
            canal.basic_consume(queue=QUEUE, on_message_callback=_procesar_mensaje)

            logger.info(f"Consumidor RabbitMQ listo y escuchando en la cola '{QUEUE}'")
            canal.start_consuming()
        except pika.exceptions.AMQPConnectionError as e:
            logger.warning(f"Error de conexión con RabbitMQ: {e}. Reintentando en 5 segundos...")
            time.sleep(5)
        except Exception as e:
            logger.error(f"Excepción en consumidor RabbitMQ: {e}. Reintentando en 5 segundos...", exc_info=True)
            time.sleep(5)


def iniciar_consumidor_hilo():
    hilo = threading.Thread(target=iniciar_consumidor, daemon=True, name="RabbitMQConsumerThread")
    hilo.start()
    return hilo
