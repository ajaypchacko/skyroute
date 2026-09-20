package com.skyroute.skyroute.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Spring Boot creates any NewTopic beans in Kafka at startup (if they don't exist yet).
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic flightEventsTopic() {
        return TopicBuilder.name(KafkaTopics.FLIGHT_EVENTS)
                .partitions(3)   // 3 lanes; events with the same key always use the same lane
                .replicas(1)     // we only have one Kafka broker
                .build();
    }

    @Bean
    public NewTopic disruptionsTopic() {
        return TopicBuilder.name(KafkaTopics.DISRUPTIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Dead-letter topics carry very little traffic, so one partition is enough.
    @Bean
    public NewTopic flightEventsDeadLetterTopic() {
        return TopicBuilder.name(KafkaTopics.FLIGHT_EVENTS_DLT).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic disruptionsDeadLetterTopic() {
        return TopicBuilder.name(KafkaTopics.DISRUPTIONS_DLT).partitions(1).replicas(1).build();
    }
}