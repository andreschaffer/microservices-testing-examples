package welcomememberemailservice.bootstrap;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.HashMap;
import java.util.Map;

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
        configs.put("enable.auto.commit", "false");
        configs.put("auto.commit.interval.ms", "1000");
        configs.put("auto.offset.reset", "earliest");
        configs.put("session.timeout.ms", "30000");
        configs.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.putAll(this.configs);
        return configs;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }
}
