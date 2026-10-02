"""Servidor gRPC que expone ObtenerPreguntaCompleta.
"""
from dotenv import load_dotenv
load_dotenv()
from concurrent import futures

import grpc

from app.infrastructure.db.session import SessionLocal
from app.infrastructure.db.repositorio_pregunta_sqlalchemy import RepositorioPreguntaSQLAlchemy

# Generados por protoc (no existen hasta correr el comando de generación)
from app.infrastructure.grpc import pregunta_pb2, pregunta_pb2_grpc


class PreguntaServiceServicer(pregunta_pb2_grpc.PreguntaServiceServicer):

    def ObtenerPreguntaCompleta(self, request, context):
        db = SessionLocal()
        try:
            repo = RepositorioPreguntaSQLAlchemy(db)
            pregunta = repo.obtener_por_id(int(request.pregunta_id))

            if pregunta is None:
                return pregunta_pb2.PreguntaCompletaResponse(encontrada=False)

            respuesta_correcta = next(
                (d.id for d in pregunta.distractores if d.es_correcto), ""
            )
            return pregunta_pb2.PreguntaCompletaResponse(
                pregunta_id=str(pregunta.id),
                contexto=pregunta.contexto.texto,
                pregunta_directa=pregunta.pregunta_directa,
                distractores=[
                    pregunta_pb2.Distractor(id=d.id, texto=d.texto)
                    for d in pregunta.distractores
                ],
                respuesta_correcta_id=respuesta_correcta,
                justificacion=pregunta.justificacion,
                competencia=pregunta.competencia.nombre,
                tema=pregunta.competencia.tema,
                subtema=pregunta.competencia.subtema,
                nivel_dificultad=pregunta.nivel_dificultad.value,
                estado=pregunta.estado.value,
                bibliografia=pregunta.bibliografia,
                encontrada=True,
            )
        finally:
            db.close()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pregunta_pb2_grpc.add_PreguntaServiceServicer_to_server(
        PreguntaServiceServicer(), server
    )
    server.add_insecure_port("[::]:50051")
    server.start()
    print("Servidor gRPC escuchando en el puerto 50051")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()