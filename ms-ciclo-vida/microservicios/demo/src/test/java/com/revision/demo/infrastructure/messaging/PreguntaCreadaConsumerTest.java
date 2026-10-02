package com.revision.demo.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.repository.RevisionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import tools.jackson.databind.ObjectMapper;

class PreguntaCreadaConsumerTest {
    private final RevisionRepository repository = mock(RevisionRepository.class);
    private final PreguntaCreadaConsumer consumer = new PreguntaCreadaConsumer(
        new ObjectMapper(),
        repository
    );

    @Test
    void debeCrearRevisionInicialAlRecibirPreguntaCreada() throws Exception {
        Revision revision = Revision.pendiente(UUID.randomUUID().toString(), 73L);
        org.mockito.Mockito.when(repository.registrarPreguntaCreada(73L)).thenReturn(revision);
        String evento = """
            {
              "evento": "PreguntaCreada",
              "version": 1,
              "timestamp": "2026-10-02T14:00:00Z",
              "data": {
                "pregunta_id": "73",
                "autor_id": "u-001",
                "estado": "PENDIENTE_REVISION"
              }
            }
            """;

        consumer.procesar(evento.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        verify(repository).registrarPreguntaCreada(73L);
    }

    @Test
    void debeEnviarADeadLetterEventosFueraDelContrato() {
        String evento = """
            {
              "evento": "PreguntaCreada",
              "version": 1,
              "timestamp": "2026-10-02T14:00:00Z",
              "data": {"pregunta_id": "73", "estado": "BORRADOR"}
            }
            """;

        assertThrows(AmqpRejectAndDontRequeueException.class, () ->
            consumer.recibir(evento.getBytes(java.nio.charset.StandardCharsets.UTF_8))
        );
    }
}
