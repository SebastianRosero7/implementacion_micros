from dotenv import load_dotenv
load_dotenv()

from contextlib import asynccontextmanager
from fastapi import FastAPI

from app.infrastructure.db.models import Base
from app.infrastructure.db.session import engine
from app.infrastructure.messaging.rabbitmq_consumer import iniciar_consumidor_hilo
from app.interfaces.rest.router import router as preguntas_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Inicia el consumidor de eventos RabbitMQ en segundo plano
    iniciar_consumidor_hilo()
    yield


app = FastAPI(title="MS-1: Banco de Preguntas", lifespan=lifespan)

Base.metadata.create_all(bind=engine)

app.include_router(preguntas_router)


@app.get("/health")
def health():
    return {"status": "ok", "service": "banco-preguntas"}