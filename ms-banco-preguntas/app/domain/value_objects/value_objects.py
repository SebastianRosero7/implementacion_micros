from dataclasses import dataclass
from enum import Enum


class NivelDificultad(str, Enum):
    BAJO = "BAJO"
    MEDIO = "MEDIO"
    ALTO = "ALTO"


class EstadoPregunta(str, Enum):
    BORRADOR = "BORRADOR"
    PENDIENTE_REVISION = "PENDIENTE_REVISION"
    EN_REVISION = "EN_REVISION"
    APROBADA = "APROBADA"
    RECHAZADA = "RECHAZADA"
    PUBLICADA = "PUBLICADA"
    ARCHIVADA = "ARCHIVADA"


@dataclass(frozen=True)
class Distractor:
    id: str
    texto: str
    es_correcto: bool


@dataclass(frozen=True)
class Contexto:
    texto: str

    def __post_init__(self):
        if not self.texto or not self.texto.strip():
            raise ValueError("El contexto no puede estar vacío")


@dataclass(frozen=True)
class Competencia:
    nombre: str
    tema: str
    subtema: str