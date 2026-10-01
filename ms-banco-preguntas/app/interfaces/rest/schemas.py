from pydantic import BaseModel


class DistractorSchema(BaseModel):
    id: str
    texto: str
    es_correcto: bool


class CrearPreguntaSchema(BaseModel):
    contexto: str
    pregunta_directa: str
    distractores: list[DistractorSchema]
    justificacion: str
    competencia: str
    tema: str
    subtema: str
    nivel_dificultad: str
    bibliografia: str = ""


class EditarPreguntaSchema(BaseModel):
    contexto: str | None = None
    pregunta_directa: str | None = None


class PreguntaResponseSchema(BaseModel):
    id: int
    contexto: str
    pregunta_directa: str
    distractores: list[DistractorSchema]
    justificacion: str
    competencia: str
    nivel_dificultad: str
    estado: str
    bibliografia: str