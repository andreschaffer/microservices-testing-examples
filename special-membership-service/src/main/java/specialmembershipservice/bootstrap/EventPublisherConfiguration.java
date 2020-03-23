package specialmembershipservice.bootstrap;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BATCH_SIZE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BUFFER_MEMORY_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.apache.kafka.common.serialization.StringSerializer;

public class EventPublisherConfiguration {

  @NotBlank
  private String topic;

  @NotEmpty
  private Map<String, Object> configs;

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public Map<String, Object> getConfigs() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ACKS_CONFIG, "all");
    configs.put(RETRIES_CONFIG, 1);
    configs.put(BATCH_SIZE_CONFIG, 16384);
    configs.put(LINGER_MS_CONFIG, 1);
    configs.put(BUFFER_MEMORY_CONFIG, 33554432);
    configs.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.putAll(this.configs);
    return configs;
  }

  public void setConfigs(Map<String, Object> configs) {
    this.configs = configs;
  }
}
