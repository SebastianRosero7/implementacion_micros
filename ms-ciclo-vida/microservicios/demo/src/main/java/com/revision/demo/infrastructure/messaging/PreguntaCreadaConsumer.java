package com.revision.demo.infrastructure.messaging;

import com.revision.demo.domain.repository.RevisionRepository;
import java.time.Instant;
import java.time.DateTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class PreguntaCreadaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreguntaCreadaConsumer.class);
    private static final TypeReference<DomainEvent<PreguntaCreadaData>> EVENT_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final RevisionRepository revisionRepository;

    public PreguntaCreadaConsumer(ObjectMapper objectMapper, RevisionRepository revisionRepository) {
        this.objectMapper = objectMapper;
        this.revisionRepository = revisionRepository;
    }

    @RabbitListener(queues = RabbitMessagingConfig.QUESTION_CREATED_QUEUE)
    public void recibir(byte[] body) {
        try {
            procesar(body);
        } catch (JacksonException | IllegalArgumentException exception) {
            LOGGER.error("Evento PreguntaCreada inválido; se enviará a la dead-letter queue", exception);
            throw new AmqpRejectAndDontRequeueException("Evento PreguntaCreada inválido", exception);
        }
    }

    void procesar(byte[] body) throws JacksonException {
        DomainEvent<PreguntaCreadaData> evento = objectMapper.readValue(body, EVENT_TYPE);
        if (evento == null
            || !"PreguntaCreada".equals(evento.evento())
            || evento.version() != 1
            || evento.data() == null
            || evento.timestamp() == null) {
            throw new IllegalArgumentException("El mensaje no cumple el contrato de PreguntaCreada v1");
        }
        try {
            Instant.parse(evento.timestamp());
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("El timestamp debe ser una fecha ISO-8601 válida", exception);
        }

        PreguntaCreadaData data = evento.data();
        if (data.pregunta_id() == null || data.estado() == null) {
            throw new IllegalArgumentException("El ID y el estado de la pregunta son obligatorios");
        }
        long idPregunta;
        try {
            idPregunta = Long.parseLong(data.pregunta_id());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El ID de pregunta debe ser numérico", exception);
        }
        if (idPregunta <= 0 || !"PENDIENTE_REVISION".equals(data.estado())) {
            throw new IllegalArgumentException(
                "Solo se crean revisiones para IDs positivos en estado PENDIENTE_REVISION"
            );
        }

        var revision = revisionRepository.registrarPreguntaCreada(idPregunta);
        LOGGER.info(
            "Evento PreguntaCreada procesado para pregunta {} (revisión {})",
            idPregunta,
            revision.getId()
        );
    }

    public record DomainEvent<T>(String evento, int version, String timestamp, T data) {}

    public record PreguntaCreadaData(
        String pregunta_id,
        String autor_id,
        String competencia,
        String tema,
        String subtema,
        String nivel_dificultad,
        String estado
    ) {}
}
