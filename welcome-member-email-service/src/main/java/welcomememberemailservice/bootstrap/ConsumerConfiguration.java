package welcomememberemailservice.bootstrap;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class ConsumerConfiguration {

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
        configs.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        configs.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        configs.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(SESSION_TIMEOUT_MS_CONFIG, "30000");
        configs.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.putAll(this.configs);
        return configs;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }
}
