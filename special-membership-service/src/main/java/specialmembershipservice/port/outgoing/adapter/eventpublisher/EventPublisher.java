package specialmembershipservice.port.outgoing.adapter.eventpublisher;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
      producer.send(new ProducerRecord<>(topic, message)).get(3, SECONDS);
    } catch (Exception e) {
      LOG.error("Failed to publish message = {} to topic = {}", message, topic, e);
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
