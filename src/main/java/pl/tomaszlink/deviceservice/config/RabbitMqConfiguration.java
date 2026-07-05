package pl.tomaszlink.deviceservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
            @Value("${app.rabbitmq.queue}") String queueName,
            @Value("${app.rabbitmq.dlx}") String dlxName,
            @Value("${app.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
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
    public DirectExchange deviceLocationsDlx(
            @Value("${app.rabbitmq.dlx}") String dlxName
    ) {
        return new DirectExchange(dlxName, true, false);
    }

    @Bean
    public Queue deviceLocationsDlq(
            @Value("${app.rabbitmq.dlq}") String dlqName
    ) {
        return new Queue(dlqName, true);
    }

    @Bean
    public Binding deviceLocationsDlqBinding(
            Queue deviceLocationsDlq,
            DirectExchange deviceLocationsDlx,
            @Value("${app.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        return BindingBuilder
                .bind(deviceLocationsDlq)
                .to(deviceLocationsDlx)
                .with(dlqRoutingKey);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
