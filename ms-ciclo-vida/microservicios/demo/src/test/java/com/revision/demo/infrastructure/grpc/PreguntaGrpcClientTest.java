package com.revision.demo.infrastructure.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.unicauca.cicloVidaRevision.grpc.Distractor;
import co.unicauca.cicloVidaRevision.grpc.ObtenerPreguntaRequest;
import co.unicauca.cicloVidaRevision.grpc.PreguntaCompletaResponse;
import co.unicauca.cicloVidaRevision.grpc.PreguntaServiceGrpc;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class PreguntaGrpcClientTest {
    @Test
    void debeConsultarYMapearLaPreguntaCompleta() {
        var stub = mock(PreguntaServiceGrpc.PreguntaServiceBlockingStub.class);
        when(stub.withDeadlineAfter(anyLong(), any(TimeUnit.class)))
            .thenReturn(stub);
        when(stub.obtenerPreguntaCompleta(any(ObtenerPreguntaRequest.class)))
            .thenReturn(PreguntaCompletaResponse.newBuilder()
                .setPreguntaId("73")
                .setContexto("Contexto")
                .setPreguntaDirecta("Pregunta")
                .addDistractores(Distractor.newBuilder().setId("A").setTexto("Opción A").build())
                .setRespuestaCorrectaId("A")
                .setJustificacion("Justificación")
                .setCompetencia("Lectura crítica")
                .setTema("Comprensión")
                .setSubtema("Inferencias")
                .setNivelDificultad("MEDIO")
                .setEstado("PENDIENTE_REVISION")
                .setBibliografia("")
                .setEncontrada(true)
                .build());

        var pregunta = new PreguntaGrpcClient(stub).obtenerCompleta(73L);

        assertEquals("73", pregunta.preguntaId());
        assertEquals("Opción A", pregunta.distractores().get(0).texto());
        assertEquals("PENDIENTE_REVISION", pregunta.estado());
    }

    @Test
    void debeReportarPreguntaInexistente() {
        var stub = mock(PreguntaServiceGrpc.PreguntaServiceBlockingStub.class);
        when(stub.withDeadlineAfter(anyLong(), any(TimeUnit.class))).thenReturn(stub);
        when(stub.obtenerPreguntaCompleta(any(ObtenerPreguntaRequest.class)))
            .thenReturn(PreguntaCompletaResponse.newBuilder().setEncontrada(false).build());

        assertThrows(NoSuchElementException.class, () ->
            new PreguntaGrpcClient(stub).obtenerCompleta(73L)
        );
    }
}
