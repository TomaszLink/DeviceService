package pl.tomaszlink.deviceservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    @Bean
    public DirectExchange deviceLocationsExchange(
            @Value("${app.rabbitmq.exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue deviceLocationsQueue(
            @Value("${app.rabbitmq.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding deviceLocationsBinding(
            Queue deviceLocationsQueue,
            DirectExchange deviceLocationsExchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder
                .bind(deviceLocationsQueue)
                .to(deviceLocationsExchange)
                .with(routingKey);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
