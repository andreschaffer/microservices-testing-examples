package welcomememberemailservice.it.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class KafkaConsumerOffsets {

    private final KafkaConsumer<Object, Object> consumer;

    public KafkaConsumerOffsets(String host, Integer port, String groupId) {
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(GROUP_ID_CONFIG, groupId);

        this.consumer = new KafkaConsumer<>(properties);
    }

    public long readOffset(String topic) {
        return readOffset(topic, 0);
    }

    public long readOffset(String topic, int partition) {
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        return Optional.ofNullable(consumer.committed(Set.of(topicPartition)).get(topicPartition))
                .map(OffsetAndMetadata::offset)
                .orElse(0L);
    }
}
