from sqlalchemy.orm import Session

from app.domain.entities.pregunta import Pregunta
from app.domain.repositories.repositorio_pregunta import RepositorioPregunta
from app.domain.value_objects.value_objects import (
    Contexto, Distractor, Competencia, NivelDificultad, EstadoPregunta,
)
from app.infrastructure.db.models import PreguntaModel


class RepositorioPreguntaSQLAlchemy(RepositorioPregunta):

    def __init__(self, db: Session):
        self.db = db

    def guardar(self, pregunta: Pregunta) -> Pregunta:
        if pregunta.id is None:
            modelo = PreguntaModel()
        else:
            modelo = self.db.get(PreguntaModel, pregunta.id)

        modelo.contexto = pregunta.contexto.texto
        modelo.pregunta_directa = pregunta.pregunta_directa
        modelo.distractores = [
            {"id": d.id, "texto": d.texto, "es_correcto": d.es_correcto}
            for d in pregunta.distractores
        ]
        modelo.justificacion = pregunta.justificacion
        modelo.competencia = pregunta.competencia.nombre
        modelo.tema = pregunta.competencia.tema
        modelo.subtema = pregunta.competencia.subtema
        modelo.nivel_dificultad = pregunta.nivel_dificultad.value
        modelo.bibliografia = pregunta.bibliografia
        modelo.estado = pregunta.estado.value

        self.db.add(modelo)
        self.db.commit()
        self.db.refresh(modelo)  # trae el id autoincremental asignado

        pregunta.id = modelo.id
        return pregunta

    def obtener_por_id(self, pregunta_id: int) -> Pregunta | None:
        modelo = self.db.get(PreguntaModel, pregunta_id)
        return self._a_entidad(modelo) if modelo else None

    def listar(self) -> list[Pregunta]:
        return [self._a_entidad(m) for m in self.db.query(PreguntaModel).all()]

    @staticmethod
    def _a_entidad(modelo: PreguntaModel) -> Pregunta:
        return Pregunta(
            id=modelo.id,
            contexto=Contexto(texto=modelo.contexto),
            pregunta_directa=modelo.pregunta_directa,
            distractores=[Distractor(**d) for d in modelo.distractores],
            justificacion=modelo.justificacion,
            competencia=Competencia(
                nombre=modelo.competencia, tema=modelo.tema, subtema=modelo.subtema
            ),
            nivel_dificultad=NivelDificultad(modelo.nivel_dificultad),
            bibliografia=modelo.bibliografia,
            estado=EstadoPregunta(modelo.estado),
        )