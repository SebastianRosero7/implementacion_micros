from abc import ABC, abstractmethod

from app.domain.entities.pregunta import Pregunta


class RepositorioPregunta(ABC):

    @abstractmethod
    def guardar(self, pregunta: Pregunta) -> Pregunta: ...

    @abstractmethod
    def obtener_por_id(self, pregunta_id: int) -> Pregunta | None: ...

    @abstractmethod
    def listar(self) -> list[Pregunta]: ...