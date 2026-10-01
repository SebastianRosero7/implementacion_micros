from sqlalchemy import Column, Integer, String, JSON
from sqlalchemy.orm import declarative_base

Base = declarative_base()


class PreguntaModel(Base):
    __tablename__ = "preguntas"

    id = Column(Integer, primary_key=True, autoincrement=True)
    contexto = Column(String, nullable=False)
    pregunta_directa = Column(String, nullable=False)
    distractores = Column(JSON, nullable=False)  # [{id, texto, es_correcto}, ...]
    justificacion = Column(String, nullable=False)
    competencia = Column(String, nullable=False)
    tema = Column(String, nullable=False)
    subtema = Column(String, nullable=False)
    nivel_dificultad = Column(String, nullable=False)
    bibliografia = Column(String, default="")
    estado = Column(String, nullable=False, default="BORRADOR")