"""Casos de uso CU-03 (crear), CU-04 (editar), consulta y listado."""
from app.domain.entities.pregunta import Pregunta
from app.domain.repositories.repositorio_pregunta import RepositorioPregunta
from app.domain.services.validador_estructural import ValidadorEstructural
from app.domain.value_objects.value_objects import (
    Contexto, Distractor, Competencia, NivelDificultad,
)
from app.infrastructure.messaging.rabbitmq_publisher import RabbitMQPublisher


class GestionarPregunta:
    """Un único caso de uso concentra crear/editar/consultar/listar
    para evitar dispersar la orquestación en varias clases pequeñas."""

    def __init__(
        self,
        repositorio: RepositorioPregunta,
        validador: ValidadorEstructural,
        publisher: RabbitMQPublisher,
    ):
        self.repositorio = repositorio
        self.validador = validador
        self.publisher = publisher

    def crear(self, datos: dict) -> Pregunta:
        distractores = [
            Distractor(id=d["id"], texto=d["texto"], es_correcto=d["es_correcto"])
            for d in datos["distractores"]
        ]
        pregunta = Pregunta.crear(
            contexto=Contexto(texto=datos["contexto"]),
            pregunta_directa=datos["pregunta_directa"],
            distractores=distractores,
            justificacion=datos["justificacion"],
            competencia=Competencia(
                nombre=datos["competencia"],
                tema=datos["tema"],
                subtema=datos["subtema"],
            ),
            nivel_dificultad=NivelDificultad(datos["nivel_dificultad"]),
            bibliografia=datos.get("bibliografia", ""),
        )

        # Se guarda primero: el id es incremental y solo existe
        # después de que la base de datos lo asigna.
        pregunta = self.repositorio.guardar(pregunta)

        # Evento de dominio -> RabbitMQ (contracts/eventos.md)
        self.publisher.publicar(
            routing_key="pregunta.creada",
            evento="PreguntaCreada",
            data={
                "pregunta_id": str(pregunta.id),
                "competencia": pregunta.competencia.nombre,
                "tema": pregunta.competencia.tema,
                "subtema": pregunta.competencia.subtema,
                "nivel_dificultad": pregunta.nivel_dificultad.value,
                "estado": pregunta.estado.value,
            },
        )
        return pregunta

    def editar(self, pregunta_id: int, cambios: dict) -> Pregunta:
        pregunta = self.repositorio.obtener_por_id(pregunta_id)
        if pregunta is None:
            raise ValueError("Pregunta no encontrada")
        pregunta.editar(**cambios)
        return self.repositorio.guardar(pregunta)

    def consultar(self, pregunta_id: int) -> Pregunta | None:
        return self.repositorio.obtener_por_id(pregunta_id)

    def listar(self) -> list[Pregunta]:
        return self.repositorio.listar()