package com.revision.demo.infrastructure.messaging;

import com.revision.demo.application.port.DictamenPublisher;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitDictamenPublisher implements DictamenPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitDictamenPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitDictamenPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(Revision revision, TipoDictamen tipo, String justificacion) {
        String routingKey = tipo == TipoDictamen.APROBADA
            ? RabbitMessagingConfig.QUESTION_APPROVED_ROUTING_KEY
            : RabbitMessagingConfig.QUESTION_REJECTED_ROUTING_KEY;
        String evento = tipo == TipoDictamen.APROBADA ? "PreguntaAprobada" : "PreguntaRechazada";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pregunta_id", String.valueOf(revision.getIdPregunta()));
        data.put("revision_id", revision.getId());
        data.put("revisor_id", revision.getIdRevisor() == null ? "" : String.valueOf(revision.getIdRevisor()));
        data.put("resultado", tipo.name());
        data.put("observaciones", justificacion == null ? "" : justificacion);

        Map<String, Object> mensaje = new LinkedHashMap<>();
        mensaje.put("evento", evento);
        mensaje.put("version", 1);
        mensaje.put("timestamp", Instant.now().toString());
        mensaje.put("data", data);

        try {
            byte[] body = objectMapper.writeValueAsBytes(mensaje);
            rabbitTemplate.convertAndSend(RabbitMessagingConfig.EVENTS_EXCHANGE, routingKey, body);
            LOGGER.info("Evento {} publicado para pregunta {} (routing key: {})", evento, revision.getIdPregunta(), routingKey);
        } catch (Exception e) {
            LOGGER.error("Error al publicar evento " + evento, e);
        }
    }
}
