package specialmembershipservice.it.kafka;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerExtension implements BeforeAllCallback, AfterAllCallback {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String host;
  private int port;
  private KafkaContainer kafka;

  public KafkaContainerExtension(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));
    kafka.setPortBindings(
        List.of(String.format("%s:%s", port, port), String.format("%s:%s", 9093, 9093)));
    kafka.withListener(() -> String.format("%s:%s", host, port));
    kafka.withEmbeddedZookeeper().start();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    if (kafka != null) {
      kafka.stop();
    }
  }

  public void produceStrings(String topic, String message) {
    try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfigs())) {
      producer.send(new ProducerRecord<>(topic, message)).get(3, SECONDS);
    } catch (Exception e) {
      LOG.error("Failed to publish message = {} to topic = {}", message, topic, e);
    }
  }

  public String consumeString(String topic, Duration pollTimeout) {
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfigs())) {
      consumer.subscribe(singletonList(topic));
      return readMessage(consumer, pollTimeout);
    }
  }

  private String readMessage(KafkaConsumer<String, String> consumer, Duration pollTimeout) {
    String polledMessage = null;

    while (polledMessage == null && !Thread.currentThread().isInterrupted()) {
      ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
      for (ConsumerRecord<String, String> rec : records) {
        polledMessage = rec.value();

        Map<TopicPartition, OffsetAndMetadata> commitBuffer = new HashMap<>();
        commitBuffer.put(
            new TopicPartition(rec.topic(), rec.partition()),
            new OffsetAndMetadata(rec.offset() + 1)
        );

        consumer.commitSync(commitBuffer);
        break;
      }
    }

    return polledMessage;
  }

  private Properties producerConfigs() {
    Properties configs = new Properties();
    configs.put("bootstrap.servers", host + ":" + port);
    configs.put("acks", "1");
    configs.put("batch.size", "10");
    configs.put("client.id", "kafka-testcontainers-producer");
    configs.put("request.timeout.ms", "500");
    configs.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return configs;
  }

  private Map<String, Object> consumerConfigs() {
    Map<String, Object> configs = new HashMap<>();
    configs.put("bootstrap.servers", host + ":" + port);
    configs.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
    configs.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    configs.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    configs.put("group.id", "kafka-testcontainers-consumer");
    configs.put(SESSION_TIMEOUT_MS_CONFIG, "30000");
    configs.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return configs;
  }

  public int getPort() {
    return port;
  }
}
