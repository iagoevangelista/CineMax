package com.cinemax.facturacion.messaging;

import com.cinemax.facturacion.dto.external.VentaRealizadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentaEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    public void publicarVentaRealizada(VentaRealizadaEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.VENTA_EXCHANGE,
                    RabbitMQConfig.VENTA_REALIZADA_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            // Un fallo en RabbitMQ NO debe tumbar una venta ya confirmada en la BD
            log.error("No se pudo publicar el evento VentaRealizada para la transacción {}",
                    event.getIdTransaction(), e);
        }
    }
}