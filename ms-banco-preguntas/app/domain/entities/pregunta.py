import uuid
from dataclasses import dataclass, field

from app.domain.value_objects.value_objects import (
    Contexto, Distractor, Competencia, NivelDificultad, EstadoPregunta,
)

TEXTOS_PROHIBIDOS = {"todas las anteriores", "ninguna de las anteriores"}


class PreguntaInvalidaError(Exception):
    pass


@dataclass
class Pregunta:
    id: int | None
    contexto: Contexto
    pregunta_directa: str
    distractores: list[Distractor]
    justificacion: str
    competencia: Competencia
    nivel_dificultad: NivelDificultad
    bibliografia: str = ""
    estado: EstadoPregunta = EstadoPregunta.BORRADOR

    @staticmethod
    def crear(
        contexto: Contexto,
        pregunta_directa: str,
        distractores: list[Distractor],
        justificacion: str,
        competencia: Competencia,
        nivel_dificultad: NivelDificultad,
        bibliografia: str = "",
    ) -> "Pregunta":
        pregunta = Pregunta(
            id=None,
            contexto=contexto,
            pregunta_directa=pregunta_directa,
            distractores=distractores,
            justificacion=justificacion,
            competencia=competencia,
            nivel_dificultad=nivel_dificultad,
            bibliografia=bibliografia,
        )
        pregunta._validar_invariantes()
        return pregunta

    def editar(self, **cambios) -> None:
        # Invariante RF-06: solo editable en Borrador
        if self.estado != EstadoPregunta.BORRADOR:
            raise PreguntaInvalidaError(
                "Solo se puede editar una pregunta en estado BORRADOR"
            )
        for campo, valor in cambios.items():
            setattr(self, campo, valor)
        self._validar_invariantes()

    def marcar_pendiente_revision(self) -> None:
        self._validar_invariantes()
        self.estado = EstadoPregunta.PENDIENTE_REVISION

    def _validar_invariantes(self) -> None:
        # RF-08/RF-09: exactamente 1 contexto y 1 pregunta directa
        if not self.pregunta_directa or not self.pregunta_directa.strip():
            raise PreguntaInvalidaError("La pregunta directa no puede estar vacía")

        # RF-10/RF-11: exactamente 4 distractores y 1 respuesta correcta
        if len(self.distractores) != 4:
            raise PreguntaInvalidaError("Debe haber exactamente 4 distractores")
        correctos = [d for d in self.distractores if d.es_correcto]
        if len(correctos) != 1:
            raise PreguntaInvalidaError("Debe existir exactamente 1 respuesta correcta")

        # RF-12: sin "todas/ninguna de las anteriores"
        for d in self.distractores:
            if d.texto.strip().lower() in TEXTOS_PROHIBIDOS:
                raise PreguntaInvalidaError(
                    f"No se permite el distractor: '{d.texto}'"
                )