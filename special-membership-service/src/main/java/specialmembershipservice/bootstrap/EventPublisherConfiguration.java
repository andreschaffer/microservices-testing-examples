package specialmembershipservice.bootstrap;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.HashMap;
import java.util.Map;

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
        configs.put("acks", "all");
        configs.put("retries", 1);
        configs.put("batch.size", 16384);
        configs.put("linger.ms", 1);
        configs.put("buffer.memory", 33554432);
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.putAll(this.configs);
        return configs;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }
}
