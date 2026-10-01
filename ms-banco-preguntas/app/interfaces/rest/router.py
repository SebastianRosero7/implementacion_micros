from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.application.use_cases.gestionar_pregunta import GestionarPregunta
from app.domain.entities.pregunta import Pregunta, PreguntaInvalidaError
from app.domain.services.validador_estructural import ValidadorEstructural
from app.infrastructure.db.repositorio_pregunta_sqlalchemy import RepositorioPreguntaSQLAlchemy
from app.infrastructure.db.session import get_db
from app.infrastructure.messaging.rabbitmq_publisher import RabbitMQPublisher
from app.interfaces.rest.schemas import (
    CrearPreguntaSchema, EditarPreguntaSchema, PreguntaResponseSchema,
)

router = APIRouter(prefix="/preguntas", tags=["preguntas"])


def _caso_de_uso(db: Session = Depends(get_db)) -> GestionarPregunta:
    return GestionarPregunta(
        repositorio=RepositorioPreguntaSQLAlchemy(db),
        validador=ValidadorEstructural(),
        publisher=RabbitMQPublisher(),
    )


def _a_response(p: Pregunta) -> PreguntaResponseSchema:
    return PreguntaResponseSchema(
        id=p.id,
        contexto=p.contexto.texto,
        pregunta_directa=p.pregunta_directa,
        distractores=[d.__dict__ for d in p.distractores],
        justificacion=p.justificacion,
        competencia=p.competencia.nombre,
        nivel_dificultad=p.nivel_dificultad.value,
        estado=p.estado.value,
        bibliografia=p.bibliografia,
    )


@router.post("", response_model=PreguntaResponseSchema, status_code=201)
def crear_pregunta(body: CrearPreguntaSchema, caso: GestionarPregunta = Depends(_caso_de_uso)):
    try:
        pregunta = caso.crear(body.model_dump())
    except PreguntaInvalidaError as e:
        raise HTTPException(status_code=422, detail=str(e))
    return _a_response(pregunta)


@router.get("/{pregunta_id}", response_model=PreguntaResponseSchema)
def consultar_pregunta(pregunta_id: int, caso: GestionarPregunta = Depends(_caso_de_uso)):
    pregunta = caso.consultar(pregunta_id)
    if pregunta is None:
        raise HTTPException(status_code=404, detail="Pregunta no encontrada")
    return _a_response(pregunta)


@router.get("", response_model=list[PreguntaResponseSchema])
def listar_preguntas(caso: GestionarPregunta = Depends(_caso_de_uso)):
    return [_a_response(p) for p in caso.listar()]


@router.patch("/{pregunta_id}", response_model=PreguntaResponseSchema)
def editar_pregunta(
    pregunta_id: int, body: EditarPreguntaSchema, caso: GestionarPregunta = Depends(_caso_de_uso)
):
    try:
        cambios = {k: v for k, v in body.model_dump().items() if v is not None}
        pregunta = caso.editar(pregunta_id, cambios)
    except PreguntaInvalidaError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    return _a_response(pregunta)