from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI

from app.infrastructure.db.models import Base
from app.infrastructure.db.session import engine
from app.interfaces.rest.router import router as preguntas_router

app = FastAPI(title="MS-1: Banco de Preguntas")

Base.metadata.create_all(bind=engine)

app.include_router(preguntas_router)


@app.get("/health")
def health():
    return {"status": "ok", "service": "banco-preguntas"}