package com.cinemax.facturacion.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VENTA_EXCHANGE = "venta.exchange";
    public static final String VENTA_REALIZADA_ROUTING_KEY = "venta.realizada";
    public static final String VENTA_REALIZADA_QUEUE = "usuarios.venta-realizada.queue";

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(VENTA_EXCHANGE);
    }

    // facturacion-service declara también la cola: mientras usuarios-service no
    // exista, esto permite verificar en el panel de RabbitMQ que el mensaje
    // se está publicando correctamente.
    @Bean
    public Queue ventaRealizadaQueue() {
        return new Queue(VENTA_REALIZADA_QUEUE, true);
    }

    @Bean
    public Binding ventaRealizadaBinding(Queue ventaRealizadaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaRealizadaQueue)
                .to(ventaExchange)
                .with(VENTA_REALIZADA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}