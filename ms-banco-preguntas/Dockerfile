FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY contracts ./contracts
COPY app ./app

# Genera los stubs gRPC durante el build de la imagen,
# y corrige el import relativo que requiere ejecutarlo dentro de un paquete.
RUN python -m grpc_tools.protoc \
      -I contracts \
      --python_out=app/infrastructure/grpc \
      --grpc_python_out=app/infrastructure/grpc \
      contracts/pregunta.proto \
    && sed -i 's/^import pregunta_pb2 as pregunta__pb2/from . import pregunta_pb2 as pregunta__pb2/' \
      app/infrastructure/grpc/pregunta_pb2_grpc.py

EXPOSE 8000 50051

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]