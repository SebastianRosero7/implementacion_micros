"""Domain service: ValidadorEstructural"""
from app.domain.entities.pregunta import Pregunta, PreguntaInvalidaError


class ValidadorEstructural:
    """Centraliza la validación estructural antes de pasar a
    Pendiente de Revisión, para que esta lógica no quede dispersa
    dentro del agregado Pregunta."""

    def validar(self, pregunta: Pregunta) -> list[str]:
        errores: list[str] = []
        try:
            pregunta._validar_invariantes()
        except PreguntaInvalidaError as e:
            errores.append(str(e))

        if not pregunta.bibliografia:
            errores.append("Se recomienda registrar bibliografía de soporte")

        return errores

    def es_valida(self, pregunta: Pregunta) -> bool:
        return len(self.validar(pregunta)) == 0