"""Repositorio como interfaz pura, sin acoplar el dominio a
tecnología de persistencia concreta (RNF-12)."""
from abc import ABC, abstractmethod

from app.domain.entities.pregunta import Pregunta


class RepositorioPregunta(ABC):

    @abstractmethod
    def guardar(self, pregunta: Pregunta) -> Pregunta: ...

    @abstractmethod
    def obtener_por_id(self, pregunta_id: int) -> Pregunta | None: ...

    @abstractmethod
    def listar(self) -> list[Pregunta]: ...