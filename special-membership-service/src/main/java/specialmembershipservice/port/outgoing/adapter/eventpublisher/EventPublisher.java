package specialmembershipservice.port.outgoing.adapter.eventpublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class EventPublisher {

    private final String topic;
    private final Map<String, Object> configs;
    private final ObjectMapper objectMapper;

    public EventPublisher(String topic, Map<String, Object> configs, ObjectMapper objectMapper) {
        this.topic = checkNotNull(topic);
        this.configs = ImmutableMap.copyOf(checkNotNull(configs));
        this.objectMapper = checkNotNull(objectMapper).copy();
    }

    public void publish(Event event) {
        String message = toMessage(event);
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(configs)) {
            producer.send(new ProducerRecord<>(topic, message));
        }
    }

    private String toMessage(Event event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new Error("Should never fail to serialize event", e);
        }
    }
}
