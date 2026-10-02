package com.revision.demo.infrastructure.messaging;

import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {
    public static final String EVENTS_EXCHANGE = "banco_preguntas.eventos";
    public static final String QUESTION_CREATED_QUEUE = "ciclo_vida.pregunta_creada";
    public static final String QUESTION_CREATED_DEAD_LETTER_QUEUE = "ciclo_vida.pregunta_creada.dlq";
    public static final String QUESTION_CREATED_ROUTING_KEY = "pregunta.creada";
    public static final String QUESTION_CREATED_DEAD_LETTER_ROUTING_KEY = "pregunta.creada.dlq";

    @Bean
    public TopicExchange bancoPreguntasEventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue preguntaCreadaQueue() {
        return new Queue(QUESTION_CREATED_QUEUE, true, false, false, Map.of(
            "x-dead-letter-exchange", EVENTS_EXCHANGE,
            "x-dead-letter-routing-key", QUESTION_CREATED_DEAD_LETTER_ROUTING_KEY
        ));
    }

    @Bean
    public Queue preguntaCreadaDeadLetterQueue() {
        return new Queue(QUESTION_CREATED_DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding preguntaCreadaBinding(Queue preguntaCreadaQueue, TopicExchange bancoPreguntasEventsExchange) {
        return BindingBuilder.bind(preguntaCreadaQueue)
            .to(bancoPreguntasEventsExchange)
            .with(QUESTION_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding preguntaCreadaDeadLetterBinding(
        Queue preguntaCreadaDeadLetterQueue,
        TopicExchange bancoPreguntasEventsExchange
    ) {
        return BindingBuilder.bind(preguntaCreadaDeadLetterQueue)
            .to(bancoPreguntasEventsExchange)
            .with(QUESTION_CREATED_DEAD_LETTER_ROUTING_KEY);
    }
}
