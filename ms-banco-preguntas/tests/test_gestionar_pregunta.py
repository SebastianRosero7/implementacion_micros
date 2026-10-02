import unittest
from unittest.mock import Mock

from app.application.use_cases.gestionar_pregunta import GestionarPregunta
from app.domain.entities.pregunta import Pregunta
from app.domain.services.validador_estructural import ValidadorEstructural
from app.domain.value_objects.value_objects import EstadoPregunta


class GestionarPreguntaTest(unittest.TestCase):
    def test_crear_publica_evento_con_estado_pendiente_revision(self):
        repositorio = Mock()
        repositorio.guardar.side_effect = lambda pregunta: Pregunta(
            id=41,
            contexto=pregunta.contexto,
            pregunta_directa=pregunta.pregunta_directa,
            distractores=pregunta.distractores,
            justificacion=pregunta.justificacion,
            competencia=pregunta.competencia,
            nivel_dificultad=pregunta.nivel_dificultad,
            bibliografia=pregunta.bibliografia,
            estado=pregunta.estado,
        )
        publisher = Mock()
        caso = GestionarPregunta(repositorio, ValidadorEstructural(), publisher)

        pregunta = caso.crear({
            "contexto": "Contexto de prueba",
            "pregunta_directa": "¿Cuál opción es correcta?",
            "distractores": [
                {"id": "A", "texto": "Opción A", "es_correcto": True},
                {"id": "B", "texto": "Opción B", "es_correcto": False},
                {"id": "C", "texto": "Opción C", "es_correcto": False},
                {"id": "D", "texto": "Opción D", "es_correcto": False},
            ],
            "justificacion": "La opción A es correcta",
            "competencia": "Lectura crítica",
            "tema": "Comprensión",
            "subtema": "Inferencias",
            "nivel_dificultad": "MEDIO",
        })

        self.assertEqual(EstadoPregunta.PENDIENTE_REVISION, pregunta.estado)
        publisher.publicar.assert_called_once()
        self.assertEqual(
            "PENDIENTE_REVISION",
            publisher.publicar.call_args.kwargs["data"]["estado"],
        )


if __name__ == "__main__":
    unittest.main()
