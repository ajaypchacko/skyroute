package com.skyroute.skyroute.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What happens when a consumer fails to process a message.
 * Spring Boot picks up this DefaultErrorHandler bean and uses it for every @KafkaListener.
 */
@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> template,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        // A message that could not even be READ (broken JSON) is only raw bytes.
        // It needs a sender that writes bytes as they are, or it would be re-encoded.
        Map<String, Object> byteProps = new HashMap<>();
        byteProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        byteProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        byteProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        KafkaTemplate<String, byte[]> bytesTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(byteProps));

        // Order matters: the first entry whose class matches the message value is used.
        Map<Class<?>, KafkaOperations<? extends Object, ? extends Object>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);   // unreadable messages
        templates.put(Object.class, template);        // normal messages that failed in our code

        // Failed messages go to "<original topic>.DLT". Partition -1 = let Kafka choose.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                templates,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", -1));

        // Retry a failing message 3 more times, 1 second apart, then send it to the DLT.
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));

        // Some errors can never succeed on retry (for example an unknown flight),
        // so those go to the dead-letter topic immediately.
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}