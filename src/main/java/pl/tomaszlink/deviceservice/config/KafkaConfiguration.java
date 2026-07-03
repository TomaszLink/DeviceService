package pl.tomaszlink.deviceservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {
    @Bean
    public NewTopic deviceLocationsTopic(
            @Value("${app.kafka.topics.device-locations}") String topicName
    ) {
        return new NewTopic(topicName, 3, (short) 1);
    }
}
