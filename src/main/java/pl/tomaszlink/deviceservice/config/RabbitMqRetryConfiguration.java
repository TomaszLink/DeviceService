package pl.tomaszlink.deviceservice.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqRetryConfiguration {

    private static final int MAX_RETRIES = 2;
    private static final long INITIAL_INTERVAL_MS = 1_000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_INTERVAL_MS = 10_000;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.dlx}") String dlxName,
            @Value("${app.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // Safety net: if the recoverer itself fails to republish (e.g. broker unreachable),
        // reject without requeue instead of looping forever; the queue's own
        // x-dead-letter-exchange argument still routes the message to the DLQ.
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor(rabbitTemplate, dlxName, dlqRoutingKey));

        return factory;
    }

    private MethodInterceptor retryInterceptor(
            RabbitTemplate rabbitTemplate,
            String dlxName,
            String dlqRoutingKey
    ) {
        return RetryInterceptorBuilder.stateless()
                .maxRetries(MAX_RETRIES)
                .backOffOptions(INITIAL_INTERVAL_MS, BACKOFF_MULTIPLIER, MAX_INTERVAL_MS)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, dlxName, dlqRoutingKey))
                .build();
    }
}